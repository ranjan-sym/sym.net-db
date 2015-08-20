package net.symplifier.db;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.symplifier.db.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * The underlying structure or a model.
 *
 * <p>
 *   The ModelStructure is generated during initialization and stores all the
 *   information that is needed for an operation on the Model.
 * </p>
 * <p>
 *   The ModelStructure also keeps the cache of the records of model
 * </p>
 *
 * Created by ranjan on 8/13/15.
 */
public class ModelStructure<T extends Model> {
  /* The schema to which the model belongs */
  private final Schema schema;

  /* The class type of the model */
  private final Class<T> modelClass;

  /* The name of the table on the database for this model */
  private final String tableName;

  /* The factory object used to createModel the ModelInstance */
  private final Model.Factory<T> modelFactory;

  /* The cache of data that belongs to this model */
  private final Cache<Long, ModelRow<T>> rowCache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .build();

  /* The list of all the columns of this model, mapped by name of the column */
  private final List<Column<T, ?>> columns;
  private final Map<String, Integer> columnIndex = new HashMap<>();

  /* The list of all the references of this model */
  /* This list is not being used at the moment, and may be useful only in case
     we decide to use automatically loaded (default) references
   */
  private final Reference[] references;

  /* The hierarchy of the model. */
  private final ModelStructure[] parents;
  /* The models that have been implemented by this model */
  private final ModelStructure[] implementations;
  /* Flag to see if this structure represents a ModelInterface */
  private final boolean isModelInterface;

  /* The total number of tables that make up this model including parents and implementations */
  private final int effectiveTablesCount;

  /**
   * Special model structure creation for Intermediate models
   *
   * @param schema
   * @param modelClass
   * @param table
   * @param modelU
   * @param modelV
   * @param <U>
   * @param <V>
   */
  public <U extends Model, V extends Model> ModelStructure(Schema schema, Class<T> modelClass, String table, Class<U> modelU, Class<V> modelV) {
    assert(modelClass == ModelIntermediate.class);

    this.schema = schema;
    this.modelClass = modelClass;
    this.modelFactory = null;
    this.isModelInterface = false;

    tableName = table;

    parents = new ModelStructure[0];
    implementations = new ModelStructure[0];

    columns = new ArrayList<>(2);
    columns.add(new Column.BackReference<T, U>(modelU));
    columns.add(new Column.BackReference<T, V>(modelV));

    // References yes
    references = null;
    effectiveTablesCount = 1;
  }

  /**
   * Create new model structure
   * @param schema The schema instance
   * @param modelClass The type of the model
   * @param factory The factory object for creating ModelInstance
   */
  @SuppressWarnings("unchecked")
  public ModelStructure(Schema schema, Class<T> modelClass, Model.Factory<T> factory) {
    this.schema = schema;
    this.modelClass = modelClass;
    this.isModelInterface =  modelClass.isInterface();
    this.modelFactory = this.isModelInterface ? null : factory;

    // Get the name of the table from annotation if available otherwise
    // infer based on the name of the table
    Table table = modelClass.getAnnotation(Table.class);
    if (table != null) {
      tableName = table.value();
    } else {
      tableName = Schema.toDBName(modelClass.getSimpleName());
    }


    // Get the list of the parents
    Class parent = modelClass.getSuperclass();
    if (parent == ModelInstance.class || parent == Model.class) {
      parents = new ModelStructure[0];
    } else {
      ModelStructure parentStructure = schema.registerModel(parent, null);
      parents = new ModelStructure[parentStructure.parents.length + 1];
      int i = 0;
      for (; i < parents.length - 1; ++i) {
        parents[i] = parentStructure.parents[i];
      }
      parents[i] = parentStructure;
    }

    // Take all the implementations from the parents first
    Set<ModelStructure> impls = new LinkedHashSet<>();
    for (ModelStructure p : parents) {
      Collections.addAll(impls, p.implementations);
    }

    Set<Reference> references = new LinkedHashSet<>();
    columns = new ArrayList<>();

    // Get stock of all the columns that belong to the model
    for (Field f : modelClass.getDeclaredFields()) {
      int modifier = f.getModifiers();
      try {
        if (Modifier.isStatic(modifier) && Modifier.isFinal(modifier)) {
          if (Column.class.isAssignableFrom(f.getType())) {
            Column col = (Column) f.get(modelClass);
            col.setName(f.getName());
            col.onInit(this);

            columnIndex.put(col.getFieldName(), columns.size());
            columns.add(col);

          }

          if (Reference.class.isAssignableFrom(f.getType())) {
            references.add((Reference)f.get(modelClass));
          }

          // Let's see if we have any intermediate tables that need to be initialized
          if (Relation.HasMany.class.isAssignableFrom(f.getType())) {
            Relation.HasMany m = (Relation.HasMany) f.get(modelClass);
            String tbl = m.getIntermediateTable();
            if (tbl != null) {
              // need to register intermediate model as well
              schema.registerIntermediateModel(tbl, m.getSourceType().getType(), m.getTargetType().getType());
            }
          }
        }
      } catch (IllegalAccessException e) {
        // TODO Warning log
      }
    }

    this.references = references.toArray(new Reference[references.size()]);



    implementations = impls.toArray(new ModelStructure[impls.size()]);
    if (!isModelInterface) {
      // Set level on all implementations for this model structure
      for(int i=0; i<implementations.length; ++i) {
        List<Column> implColumns = implementations[i].columns;
        for(Column col:implColumns) {
          col.implementationLevel.put(this, parents.length + 1 + i);
        }
      }
    }

    this.effectiveTablesCount = 1 + parents.length + implementations.length;
  }

  public Class<T> getType() {
    return modelClass;
  }

  public ModelStructure[] getParents() {
    return parents;
  }

  public String getTableName() {
    return tableName;
  }

  public String getPrimaryKeyField() {
    return "id";
  }

  public Schema getSchema() {
    return schema;
  }

  public T create(ModelRow<T> row) {
    return modelFactory.create(this);
  }

  public T create() {
    return create(new ModelRow<>(this));
  }

  public ModelRow<T> getRow(long id) {
    try {
      return rowCache.get(id, () -> new ModelRow<>(ModelStructure.this));
    } catch (ExecutionException e) {
      return null;
    }
  }

  public T get(long id) {
    return create(getRow(id));
  }

  public int getColumnCount() {
    return columns.size();
  }

  public Column<T, ?> getColumn(int index) {
    return columns.get(index);
  }

  public Column<T, ?> getColumn(String fieldName) {
    return columns.get(columnIndex.get(fieldName));
  }

  public int getEffectiveTablesCount() {
    return effectiveTablesCount;
  }

  public Query.Builder<T> query() {
    return getSchema().query(this);
  }
}
