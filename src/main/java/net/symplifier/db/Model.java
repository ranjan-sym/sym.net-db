package net.symplifier.db;


import net.symplifier.core.application.Session;
import net.symplifier.db.exceptions.ModelException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
   * Retrieve a value for the custom column type
   *
   * @param column The column of which the value needs to be retrieved
   * @param <G> The generic type of the value
   * @param <T> The custom type of the column
   * @return The custom type value
   */
  default <G, T extends Column.CustomType<G>> T get(Column.Custom<?, G, T> column) {
    return column.getFromGeneric(get(column, column.getLevel()));
  }

  /**
   * Retrieve a referenced type model
   *
   * @param column The reference column of which the value needs to be retrieved
   * @param <U> The type of the parent model that holds this reference
   * @param <V> The type of the model that is referred by this reference
   * @return The model record as per the reference
   */
  <U extends Model, V extends Model> V getReference(Column.Reference<U, V> column);

  /**
   * Set a referenced type model
   *
   * @param column The reference column of which the value needs to be set
   * @param model The value that needs to be set for this reference
   * @param <U> The type of the parent model that holds this reference
   * @param <V> The type of the model that is referred by this reference
   */
  <U extends Model, V extends Model> void setReference(Column.Reference<U, V> column, V model);


  /**
   * Adds the model as a child to this relationship
   *
   * @param relation
   * @param model
   * @param <U>
   * @param <V>
   * @return
   */
  <U extends Model, V extends Model> V add(Relation.HasMany<U, V> relation, V model);

  /**
   * Removes the model as a child from this relationship
   *
   * @param relation
   * @param model
   * @param <U>
   * @param <V>
   */
  <U extends Model, V extends Model> void remove(Relation.HasMany<U, V> relation, V model);


  /**
   * Set the value of the given column of this model. When a value
   * is set for a model, a new copy is created, leaving the existing
   * copy as it is.
   *
   * @param column The column of which the value needs to be set for this model
   * @param level The depth of the model in the hierarchy of the concrete
   *              implementations. See {@link ModelInterface#get(Column, int)}
   *              for detail explanation.
   * @param value The value that needs to be set
   * @param <T> The type of the value
   */
  <T> void set(Column<?, T> column, int level, T value);

  default <G, T extends Column.CustomType<G>> void set(Column.Custom<?, G, T> column, T value) {
    set(column, column.getGeneric(value));
  }


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

  /**
   * Get the primary key id of this record
   *
   * @return The primary key value
   */
  Long getId();

  /**
   * Save the model on the given session. This method returns true
   * if this operation actually affected the persistent system below
   * and return false otherwise, which means that the model record
   * need not be saved.
   *
   * @param session The session to work on
   * @return {@code true} if the database was affected otherwise {@code false}
   */
  boolean save(DBSession session);

  /**
   * Save the model on the default session
   */
  default void save() {
    Schema schema = Schema.get();
    DBSession session = Session.get(schema, DBSession.class);
    save(session);

    // Let the session know that this instance was saved on this session
    // This has to be done here and not from save(DBSession) since that
    // method would be called recursively and that will create a number
    // of additional updated events. Need to check though if that would
    // be something of a desired process or if this would be
    session.saved(this);
  }


  default JSONObject toJSON() {
    return toJSON(0);
  }
  /**
   * Convert Model to JSON
   *
   * @param level The recursion depth at which the conversion is taking place
   *              should be incremented by one during recursive loading. Start
   *              with 0.
   * @return JSONObject
   */
  JSONObject toJSON(int level);

  /**
   * Keep the model in read only mode. Any attempt to change its property once
   * it is locked should throw a ModelException
   */
  void lock();

  /**
   * Release the model from read only mode and allow it to be changed
   */
  void unlock();

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
