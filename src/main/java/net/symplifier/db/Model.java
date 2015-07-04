package net.symplifier.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by ranjan on 7/3/15.
 */
public abstract class Model<T extends Row> {
  public static final Column<Long> KEY = new Column<Long>("id", Long.TYPE);

  public static final Logger LOGGER = LogManager.getLogger("DBModel");

  private final Schema schema;
  private final String name;
  private final Class<? extends Model> modelClass;
  private final Class<T> associatedRow;
  private final Model<? extends Row> parent;

  private final Driver.ModelInfo moreInfo;

  //private final String primaryKeyFieldName;
  private final Column<Long> primaryKeyColumn;

  private final Map<String, Column> columns = new LinkedHashMap<>();
  private final Map<String, Field> fields = new LinkedHashMap<>();
  private final Map<String, Field> references = new LinkedHashMap<>();


  private final Query<T> basicQuery;
  private final Query<T> selectByPrimaryKeyQuery;
  private final Condition<Long> primaryKeyCondition;

  public String getName() {
    return name;
  }

  public boolean contains(Column column) {
    if (column == Model.KEY) {
      return columns.get(column.getName()) == primaryKeyColumn;
    } else {
      return columns.get(column.getName()) == column;
    }
  }

  public Iterable<Column> getColumns() {
    return columns.values();
  }

  public Column getColumn(String name) {
    return columns.get(name);
  }

  public boolean isPrimaryKey(Column column) {
    return column == primaryKeyColumn;
  }
  public Field isField(Column column) {
    return fields.get(column.getName());
  }

  public Field isReference(Column column) {
    return references.get(column.getName());
  }

  public T createRow() throws ModelException {
    try {
      return associatedRow.newInstance();
    } catch(IllegalAccessException e) {
      throw new ModelException("Error while trying to create row instance for model " + modelClass + ". Default constructor not accessible.", e);
    } catch (InstantiationException e) {
      throw new ModelException("Error while trying to create row instance for model " + modelClass + ". Default constructor not found.", e);
    }
  }

  public Set<Map.Entry<String, Field>> getFields() {
    return fields.entrySet();
  }

  public Set<Map.Entry<String, Field>> getReferences() {
    return references.entrySet();
  }

  public Model<? extends Row> getParent() {
    return parent;
  }

  public boolean hasParent() {
    return parent != null;
  }

  public Driver.ModelInfo getMoreInfo() {
    return moreInfo;
  }

  public Model(Schema schema, String name) throws ModelException {
    this(schema, name, null);
  }

  public Model(Schema schema, String name, Model<? extends Row> parent) throws ModelException {
    if (schema.hasModel(name)) {
      throw new ModelException("Model with name " + name + " already defined in the Schema " + schema);
    }

    this.modelClass = this.getClass();
    // Let's get the superClass for this Model, the super class must always be
    // Model.class
    if (modelClass.getSuperclass() != Model.class) {
      throw new ModelException("A model defined in the system must extend directly from " + Model.class + " only");
    }

    this.schema = schema;
    this.name = name;
    this.parent = parent;


    // Add a primary key column
    String primaryKeyField= generatePrimaryKeyFieldName();
    this.primaryKeyColumn = new Column<>(primaryKeyField, Long.class);
    this.columns.put(primaryKeyField, this.primaryKeyColumn);

    // Let's find out all the columns that are defined for this model with
    // and associate it with the corresponding Row class
    this.associatedRow = updateColumns();

    // once the model is initialized register it in the schema
    moreInfo = schema.registerModel(name, this);

    basicQuery = generateBasicQuery();
    selectByPrimaryKeyQuery = basicQuery.copy();
    selectByPrimaryKeyQuery.filter(primaryKeyCondition = primaryKeyColumn.is(0L));
  }

  private Query<T> generateBasicQuery() {
    return schema.createQuery(this);
  }



  private String generatePrimaryKeyFieldName() {
    if (parent == null) {
      return "id";
    } else {
      return parent.name + "_" + parent.generatePrimaryKeyFieldName();
    }
  }

  private Class<T> updateColumns() {
    Class<T> associatedRow = null;
    // There must a associated Row class for this Model class
    Type type = modelClass.getGenericSuperclass();
    if (type instanceof ParameterizedType) {
      Class rowType = (Class)((ParameterizedType) type).getActualTypeArguments()[0];

      if (rowType == Row.class || !Row.class.isAssignableFrom(rowType)) {
        throw new ModelException("The Model definition of " + modelClass + " must be parameterized from a Row class of corresponding Model");
      } else {
        associatedRow = rowType;
      }

      // The associated Row must also follow the model hierarchy
      if (parent != null && associatedRow.getSuperclass() != parent.associatedRow) {
          throw new ModelException("The Row hierarchy and the Model hierarchy do not match. "
                  + modelClass + " has a parent " + parent.modelClass
                  + ", but the associated row " + associatedRow
                  + " doesn not extend from " + parent.associatedRow);
      }
    }

    for(Field field:modelClass.getDeclaredFields()) {
      // We are only trying to find out the columns, not bothering with anything
      // else
      if (field.getType() != Column.class) {
        continue;
      }

      int modifiers = field.getModifiers();
      // We expect the column definition fields to be public static final
      // But not mandatory, only throw a warning in case the Column Fields
      // are not defined public static final
      if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
        LOGGER.warn("The column definition in the Model class is expected to be \"static final\"");
      }

      // We found a field type, Let's match it with the row
      Column column;
      String propertyName = field.getName();
      try {
        column = (Column) field.get(this);
      } catch (IllegalAccessException e) {
        throw new ModelException("Error while trying to access column information " + field.getName() + " for " + modelClass, e);
      }

      Field associatedField;
      try {
        associatedField = associatedRow.getDeclaredField(propertyName);
      } catch (NoSuchFieldException e) {
        throw new ModelException("Could not find an associated field in " + associatedRow + " for " + modelClass + "." + propertyName);
      }

      Class assocType = associatedField.getType();
      // We found an associated field, let's make sure its the same type as what
      // is defined in the Column definition;
      // For reference type fields, the Row must use a Reference type class
      if (Row.class.isAssignableFrom(column.type)) {
        if (assocType != Reference.class) {
          throw new ModelException("The row column type must be Reference for " + propertyName + " of " + associatedRow);
        }

        // The associated Reference type must be final and constructed there
        if (!Modifier.isFinal(associatedField.getModifiers())) {
          throw new ModelException("The row column type of Reference must be 'final' for " + propertyName + " of " + associatedRow);
        }
        // for the reference type fields, we get Reference class with type
        // parameter set as per the column type
        Type t = associatedField.getGenericType();
        if (!(t instanceof ParameterizedType)) {
          throw new ModelException("The row column type of Reference must be parameterized for " + propertyName + " of " + associatedRow);
        }

        ParameterizedType pt = (ParameterizedType)t;
        if (column.type != pt.getActualTypeArguments()[0]) {
          throw new ModelException("The row column type of Reference is not parameterized with " + column.type + " for " + propertyName + " of " + associatedRow + " as declared in the corresponding Model - " + modelClass);
        }
      } else if (column.type != assocType) {
        throw new ModelException("The row column type and model definition type did not match for " + propertyName + " of " + modelClass + " in " + associatedRow);
      }

      // We want to update even the private fields
      associatedField.setAccessible(true);

      // Everything is ok, let's get the column information into the model
      columns.put(column.getName(), column);
      if (Row.class.isAssignableFrom(column.type)) {
        references.put(column.getName(), associatedField);
      } else {
        fields.put(column.getName(), associatedField);
      }
    }

    if (columns.size() == 0) {
      LOGGER.warn("No column definitions found for " + modelClass);
    }

    return associatedRow;
  }

  public String getPrimaryKeyFieldName() {
    return primaryKeyColumn.name;
  }

  public Class<? extends Row> getAssociatedRow() {
    return associatedRow;
  }

  public RowIterator<T> getAll() throws DatabaseException {
    return basicQuery.getRows();
  }

  public T find(long id) throws DatabaseException {
    primaryKeyCondition.setValue(id);
    RowIterator<T> iterator = selectByPrimaryKeyQuery.getRows();
    if (iterator.hasNext()) {
      return iterator.next();
    } else {
      return null;
    }
  }

  public Query<T> query() {
    return basicQuery.copy();
  }

  public void updatePrimaryKey(Row row, Long value) {
    row.setId(value);
  }

  public void updateField(Row row, Field field, Column col, Object value) {
    try {
      field.set(row, value);
    } catch(IllegalAccessException e) {
      throw new ModelException("Error while accessing field " + field.getName() + " of " + row.getClass());
    }
  }

  public void updateReference(Row row, Field field, Column col, Long value) {
    try {
      Reference ref = (Reference) field.get(row);
      ref.setId(schema.getModel(col.getType()).getModel(), value);
    } catch(IllegalAccessException e) {
      throw new ModelException("Error while accessing field " + field.getName() + " of " + row.getClass());
    }
  }

}
