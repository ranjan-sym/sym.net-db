package net.symplifier.db;

/**
 * The Driver interface that provides Database driver specific functionality
 * to Schema
 *
 * Created by ranjan on 7/27/15.
 */
public interface Driver {

  /**
   * Retrieve the schema to which this driver is associated
   *
   * @return The Schema instance
   */
  Schema getSchema();

  /**
   * Provides the object that sets the value of the parameter in the query
   * depending upon its type
   *
   * @param valueType The type of the object of which the setter is required
   * @return A Driver specific parameter setter
   */
  Object getParameterSetter(Class valueType);

  /**
   * Provides the object that retrieves the value of a column from the query
   * result depending upon its type
   *
   * @param valueType The type of the object of which the getter is required
   * @return A Driver specific field value getter
   */
  Object getField(Class valueType);

  /**
   * Runs a DDL to create the model structure in the database
   *
   * @param structure The structure of the model
   */
  void createModel(ModelStructure structure);

  /**
   * Create a query object from the query builder
   *
   * @param builder The builder used for creating the query
   * @param <T> The Type of the model
   * @return A query object that could be used for retrieving data from the database
   */
  <T extends Model> Query<T> createQuery(Query.Builder<T> builder);

  /**
   * Insert the data for given ModelStructure.
   * @param modelStructure Structure of data
   * @param data The data to insert
   * @return ID of inserted data
   */
  Long doInsert(ModelStructure modelStructure, Object[] data);

  /**
   * Run the given SQL and return the result
   *
   * @param sql
   * @param returnType
   * @param <T>
   * @return
   */
  <T> T runSQL(DBSession session, String sql, Class<T> returnType, Object ... sqlValues);
}
