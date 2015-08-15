package net.symplifier.db;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.symplifier.db.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
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

  /* The factory object used to create the ModelInstance */
  private final Model.Factory<T> modelFactory;

  /* The cache of data that belongs to this model */
  private final Cache<Long, ModelRow<T>> rowCache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .build();

  /* The list of all the columns of this model, mapped by name of the column */
  private final Map<String, Column> columns = new HashMap<>();

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

  /**
   * Create new model structure
   * @param schema The schema instance
   * @param modelClass The type of the model
   * @param factory The factory object for creating ModelInstance
   */
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

    // Get stock of all the columns that belong to the model
    for (Field f : modelClass.getDeclaredFields()) {
      int modifier = f.getModifiers();
      try {
        if (Modifier.isStatic(modifier) && Modifier.isFinal(modifier)) {
          if (Column.class.isAssignableFrom(f.getType())) {
            Column col = (Column) f.get(modelClass);
            col.setName(f.getName());
            col.onInit(this);

            columns.put(col.getFieldName(), col);
          }

          if (Reference.class.isAssignableFrom(f.getType())) {
            references.add((Reference)f.get(modelClass));
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
        Map<String, Column> columns = implementations[i].columns;
        for(Map.Entry<String, Column> entry:columns.entrySet()) {
          entry.getValue().implementationLevel.put(this, parents.length + 1 + i);
        }
      }
    }


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

  public T get(long id) {
    try {
      ModelRow<T> row = rowCache.get(id, new Callable<ModelRow<T>>() {
        @Override
        public ModelRow<T> call() throws Exception {
          return new ModelRow<>(ModelStructure.this);
        }
      });

      return create(row);

    } catch (ExecutionException e) {
      return null;
    }
  }

  public int getColumnCount() {
    return columns.size();
  }


}
