package net.symplifier.db;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.symplifier.db.annotations.SequencedTable;
import net.symplifier.db.annotations.Table;
import net.symplifier.db.exceptions.ModelException;
import org.json.JSONArray;
import org.json.JSONObject;

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
  private final Cache<Long, ModelRow> rowCache = CacheBuilder.newBuilder()
          .maximumSize(1000)
          .build();

  /* The list of all the columns of this model, mapped by name of the column */
  private final List<Column<T, ?>> columns;
  private final Map<String, Integer> columnIndex = new HashMap<>();

  private final String sequenceField;
  private Column sequenceColumn;

  /* The list of all the references of this model */
  /* This list is not being used at the moment, and may be useful only in case
     we decide to use automatically loaded (default) references
   */
  private final LinkedHashMap<String, Reference> references;

  /* The hierarchy of the model. */
  private final ModelStructure[] parents;

  /* The models that have been implemented by this model, Keep a map of
   * implementation that points to the column that implemented the interface */
  private final Map<ModelStructure, Column.Interface> implementations;

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
    sequenceField = null;
    parents = new ModelStructure[0];
    implementations = null;

    columns = new ArrayList<>(2);
    Column.BackReference<T, U> u = new Column.BackReference<>(modelU);
    Column.BackReference<T, V> v = new Column.BackReference<>(modelV);
    columns.add(u);
    columns.add(v);

    u.setName(schema.getModelStructure(modelU).tableName);
    v.setName(schema.getModelStructure(modelV).tableName);

    u.onInit(this);
    v.onInit(this);

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

    SequencedTable sequenced = modelClass.getAnnotation(SequencedTable.class);
    if (sequenced != null) {
      sequenceField = sequenced.sequenceField();
    } else {
      sequenceField = null;
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

    implementations = new LinkedHashMap<>();
    // Take all the implementations from the parents first
    for (ModelStructure p : parents) {
      Set<Map.Entry<ModelStructure, Column.Interface>> entries = p.implementations.entrySet();
      for(Map.Entry<ModelStructure, Column.Interface> entry:entries) {
        implementations.put(entry.getKey(), entry.getValue());
      }
    }

    references = new LinkedHashMap<>();
    columns = new ArrayList<>();

    this.effectiveTablesCount = 1 + parents.length + implementations.size();
  }

  /**
   * The entire schema building is a two stage process, in the first stage, all
   * the models and their columns are built and on the second stage, the
   * relationships are generated. We cannot do this in a single stage as there
   * will be lot of circular dependencies leading to Stack Over flow if we try
   * to do it in one stage.
   */
  void buildRelationship() {
    // Get stock of all the columns that belong to the model
    for (Field f : modelClass.getDeclaredFields()) {
      int modifier = f.getModifiers();
      try {
        if (Modifier.isStatic(modifier) && Modifier.isFinal(modifier)) {
          if (Column.class.isAssignableFrom(f.getType())) {
            Column col = (Column) f.get(null);
            col.setName(f.getName());
            col.onInit(this);

            columnIndex.put(col.getFieldName(), columns.size());
            columns.add(col);

          }

          if (Reference.class.isAssignableFrom(f.getType())) {
            Reference ref = (Reference) f.get(null);
            ref.setRelationName(f.getName());
            references.put(f.getName(), ref);
          }
        }
      } catch (IllegalAccessException e) {
        // TODO Warning log
      }
    }

    // Let's see if the sequenced field has been defined
    if (sequenceField != null) {
      sequenceColumn = getColumn(sequenceField);
      if (sequenceColumn == null) {
        throw new ModelException(this.modelClass, "A sequence field " + sequenceField + " has been defined, but a column with that name was not found in the model");
      }
    }

    if (!isModelInterface) {
      // Set level on all implementations for this model structure
      Set<ModelStructure> allImplementations = implementations.keySet();
      int i=0;
      for(ModelStructure impl:allImplementations) {
        List<Column> implColumns = impl.columns;
        for(Column col:implColumns) {
          col.implementationLevel.put(this, parents.length + 1 + i);
        }
        i+= 1;
      }
    }
  }

  void setupRelationship() {
    for(Reference reference:this.references.values()) {
      reference.onInitReference(this);
    }
  }


  /**
   * Determine if this represents a structure of a ModelInterface
   *
   * @return {@code true} if the structure is of a ModelInterface
   */
  public boolean isInterface() {
    return isModelInterface;
  }

  public Column.Interface getImplementationColumn(ModelStructure interfaceStructure) {
    return implementations.get(interfaceStructure);
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

  public T create(ModelRow row) {

    try {
      ModelInstance m = (ModelInstance) this.getType().newInstance();
      m.init(row);
      return (T)m;
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Creates an instance of the Model with default set
   *
   * @return ModelInstance
   */
  public T createDefault() {
    T res = create();
    // Go through all the columns and set them with the default
    for(Column c:columns) {
      Object def = c.getDefaultValue();
      if (def != null) {
        res.set(c, def);
      }
    }
    // Also go through all the parent columns
    for(ModelStructure p:parents) {
      List<Column> cols = p.columns;
      for(Column c:cols) {
        Object def = c.getDefaultValue();
        if (def != null) {
          res.set(c, def);
        }
      }
    }

    return res;
  }

  /**
   * Creates an instance of the Model with null for
   * all the values even if column may have defaults set
   * @return ModelInstance
   */
  public T create() {
    return create(new ModelRow(this));
  }

  public ModelRow getRow(long id) {
    try {
      // TODO if the row is not found, then probably we will have to run a query to retrieve the record
      return rowCache.get(id, () -> new ModelRow(ModelStructure.this));
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

  public List<Column<T, ?>> getColumns() {
    return columns;
  }

  public Column<T, ?> getColumn(String fieldName) {
    Integer index = columnIndex.get(fieldName);
    if (index == null) {
      // Search on the parent structures to retrieve the column recursively
      // Since we are doing it recursively, we only need to do the call on
      // the last parent
      if (parents.length == 0) {
        return null;
      } else {
        return parents[parents.length-1].getColumn(fieldName);
      }
    } else {
      return columns.get(index);
    }
  }

  /**
   * Returns the number of tables that are affected by this model. This total
   * includes the number of parent tables and the implemented tables
   *
   * @return The total number of tables are are affected by this model
   */
  public int getDependentTablesCount() {
    return effectiveTablesCount;
  }

  public Query.Builder<T> query() {
    return getSchema().query(this);
  }

  /**
   * The index in the hierarchy of the parent child on which this model falls
   *
   * @return
   */
  public int getModelLevel() {
    return parents.length;
  }

  public Collection<ModelStructure> getImplementations() {
    return implementations.keySet();
  }

  /**
   * Retrieve the Relation for the given name
   *
   * @param name Name of the relationship (camelCase)
   * @return The Relation object or {@code null}
   */
  public Reference getRelation(String name) {
    return references.get(name);
  }

  public Collection<Reference> getAllRelations() {
    return references.values();
  }

  /**
   * Check if the given column belongs to this model. This method is specially
   * used when the drivers need to find out if the column came actually from
   * the model that its working on or the one of the parent models
   *
   * @param col The column to check
   * @return {@code true} if the column belongs to this model otherwise
   *         {@code false}
   */
  public boolean containsColumn(Column col) {
    for(Column c:columns) {
      if (c == col) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "ModelStructure[" + this.getTableName() + "]";
  }

  /**
   * Update the cache with the updated Row
   * @param updatedRow
   */
  public void updateCache(ModelRow updatedRow) {
    // TODO Dilemma here, what do do during the update
    // either replace the existing model row with the new row
    // or update all the properties in the model row
    rowCache.put(updatedRow.getId(), updatedRow);
  }

  /**
   * Remove a record from the cache
   * @param deletedRow
   */
  public void removeFromCache(ModelRow deletedRow) {
    rowCache.invalidate(deletedRow.getId());
  }


  private JSONObject  metaData;       // MetaData Cache to avoid rebuilding

  public JSONObject getMetaData() {
    if (metaData != null) {
      return metaData;
    }

    metaData = new JSONObject();
    metaData.put("name", this.getTableName());
    metaData.put("parent", parents.length > 0 ? parents[0].getTableName() : null);

    JSONArray fields = new JSONArray();
    for(Column col: this.columns) {
      fields.put(col.getMetaData());
    }
    metaData.put("fields", fields);

    //metaData.put("default", this.createDefault().toJSON());
    JSONArray r = new JSONArray();
    Collection<Reference> allRelations = this.getAllRelations();
    for(Reference ref:allRelations) {
      // if the relationship is of type HasMany then encapsulate the
      // reference table name within [] in JSON
      JSONObject relation = new JSONObject();
      relation.put("name", ref.getRelationName());
      String refText = "";
      if (ref instanceof Relation.HasMany) {
        refText = ref.getTargetType().getTableName() + "[";
        ModelStructure intermediate = ref.getIntermediateTable();
        if (intermediate != null) {
          Reference backRef = ((Relation.HasMany) ref).getBackReference();
          if (backRef != null) {
            refText += backRef.getRelationName();
          }
          refText += "]";
        } else {
          refText += ref.getTargetFieldName() + "]";
        }
      } else {
        refText = ref.getTargetFieldName();
      }
      relation.put("ref", refText);
      r.put(relation);
    }

    metaData.put("relations", r);

    return metaData;
  }

  public Column getSequenceColumn() {
    return sequenceColumn;
  }
}
