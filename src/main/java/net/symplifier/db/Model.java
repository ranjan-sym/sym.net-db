package net.symplifier.db;


import net.symplifier.db.exceptions.ModelException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The interface that represents the model of the Database System.
 *
 * Created by ranjan on 7/27/15.
 */
public interface Model {

  /**
   * Retrieve the structure of this model
   *
   * @return {@link ModelStructure} object containing the total information
   *         about this model
   */
  ModelStructure getStructure();

  /**
   * Retrieve the value of the given column of this model
   *
   * @param column The column of which the value needs to be retrieved for this
   *               model
   * @param level The depth of the model in the hierarchy of the concrete
   *              implementation. For an independent model, the level value is 0.
   *              The value changes only in the case when we use multiple
   *              parent -> child hierarchy in the database table (Generalization).
   *              In which case the base table will have 0 level, the table that
   *              extends this base will have level 1 and so forth. The value
   *              will also be greater than 0 for interface table
   * @param <T> The type of the value
   * @return Value of the field
   */
  <T> T get(Column<?, T> column, int level);

  /**
   * Default implementation for retrieving the value of the given column of this
   * model. This method will not work in case of an interface model {@link ModelInterface}.
   * The {@link ModelInterface#getImplementedValue(Column)} must be used in
   * case of the ModelInterface columns.
   *
   * @param column The column of which the value needs to be retrieved
   * @param <T> The type of the value
   * @return Value of the field
   */
  default <T> T get(Column<?, T> column) {
    return get(column, column.getLevel());
  }

  /**
   * Set the value of the given column of this model.
   *
   * @param column The column of which the value needs to be set for this model
   * @param level The depth of the model in the hierarchy of the concrete
   *              implementations. See {@link ModelInterface#get(Column, int)}
   *              for detail explanation.
   * @param value The value that needs to be set
   * @param <T> The type of the value
   */
  <T> void set(Column<?, T> column, int level, T value);

  /**
   * Default implementation for setting the value of the given column of this
   * model. This method will not work in case of an interface model {@link ModelInterface}.
   * The {@link ModelInterface#setImplementedValue(Column, Object)} must be
   * used in case of the ModelInterface
   *
   * @param column The column of which the value needs to bet set for this model
   * @param value The value that needs to be set
   * @param <T> The type of the value
   */
  default <T> void set(Column<?, T> column, T value) {
    set(column, column.getLevel(), value);
  }


  //TODO implement the following events through interceptor and not here
  default void onValidateSelect() {

  }

  default void onValidateInsert() {

  }

  default void onValidateUpdate() {

  }

  default void onValidateSave() {

  }

  default void onValidateDelete() {

  }

  default void onInserted() {

  }

  default void onUpdated() {

  }

  default void onSaved() {

  }

  default void onDeleted() {

  }
//  <T> Field<T, ? , ? , ?> getField(Column<?, T> column);
//
//  <T extends Model> Reference<T> getReference(ReferenceColumn<?, T> column);


//  public static final Logger LOGGER = LogManager.getLogger(Model.class);
//
//  private final Field<Long, ?, ?, ?> primaryKeyField;
//  private final Map<Column, Field> fields = new HashMap<>();
//
//  public <T> Field<T, ?, ?, ?> getField(Column<?, T> column) {
//    return (Field<T, ?, ?, ?>)fields.get(column);
//  }
//
//  public <T extends Model> Reference<T> getReference(ReferenceColumn<?, T> column) {
//    return new Reference<>(fields.get(column));
//  }
//
//  public Model normalize() {
//    // Search in a cache to see if there is a children with the same id, if
//    // one is found return that, otherwise just return the same instance
//    return this;
//  }
//
//  public Long getId() {
//    return primaryKeyField.get();
//  }
//
//  public Model(Schema schema) {
//    // Create map of all the columns
//    ModelScheme<? extends Model> scheme = schema.getModelScheme(getClass());
//
//    primaryKeyField = scheme.getPrimaryKeyColumn().createField(schema);
//
//    for(Column col:scheme.getColumns()) {
//      fields.put(col, col.createField(schema));
//    }
//
//
////
////
////    // find out what are the elements within the model
////    Class<T> modelType = getModelType();
////
////    for(Field field:modelType.getDeclaredFields()) {
////      int modifiers = field.getModifiers();
////
////      String fieldName = field.getName();
////
////
////      // Find out all the fields of type "Column"
////      if (field.getType() == net.symplifier.db.Field.class) {
////        // Issue an warning in case the field has not been defined private
////        if (!Modifier.isPrivate(modifiers)) {
////          LOGGER.warn("Field " + fieldName + " is not 'private' in - " + modelType + ". A non private access can lead to error. Make the field private use getter/setter.");
////        }
////
////        if (!Modifier.isFinal(modifiers)) {
////          throw new ModelException("Field " + fieldName + " is not 'final' in - " + modelType + ". A field is expected to be final and consisting of a valid instance.");
////        }
////
////        // Let's see if we have the same field defined on the Model
////        try {
////          Field columnField = this.getClass().getDeclaredField(fieldName);
////
////          if (columnField.getType() != Column.class) {
////            throw new ModelException("Column " + fieldName + " in model " + this.getClass() + " is not a 'Column'");
////          }
////
////          // The type of the column and the field must be exactly same
////
////          // Issue a warning if the Column is not declared public
////          int columnModifiers = columnField.getModifiers();
////          if (!Modifier.isPublic(columnModifiers)) {
////            LOGGER.warn("Column " + fieldName + " is not 'public' in - " + getClass() + ". Public access on a column makes it available for querying");
////          }
////
////          if (!Modifier.isStatic(columnModifiers)) {
////            LOGGER.warn("Column " + fieldName + " is not 'static' in - " + getClass() + ". Static access on a column makes it available for querying");
////          }
////
////          if (!Modifier.isFinal(columnModifiers)) {
////            LOGGER.warn("Column " + fieldName + " is not 'final' in - " + getClass() + ". Final modifier keeps the code less susceptible to error in synchronous programming");
////          }
////
////          // Store the field associated with the name
////          fields.put(fieldName, field);
////
////        } catch(NoSuchFieldException e) {
////          LOGGER.warn("Column " + fieldName + " is not declared in Model " + this.getClass() + ". The field could be used but won't be available for querying and filtering");
////        }
////
////      }
////    }
//  }
//

  interface Factory<T extends Model> {
    T create(ModelStructure<T> structure);
  }

  class DefaultFactory<T extends Model> implements Factory<T> {
    private final Class<T> modelClass;
    public DefaultFactory(Class<T> modelClass) {
      this.modelClass = modelClass;
    }

    @Override
    public T create(ModelStructure structure) {
      try {
        Constructor<T> constructor = modelClass.getConstructor(ModelStructure.class);
        return constructor.newInstance(structure);
      } catch (NoSuchMethodException e) {
        throw new ModelException(modelClass, "Constructor with Structure not found", e);
      } catch (InvocationTargetException e) {
        throw new ModelException(modelClass, "Error while running constructor(ModelStructure)", e);
      } catch (InstantiationException e) {
        throw new ModelException(modelClass, "Model cannot be instantiated", e);
      } catch (IllegalAccessException e) {
        throw new ModelException(modelClass, "Constructor(ModelStructure) does not have proper access", e);
      }
    }
  }

}
