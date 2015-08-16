package net.symplifier.db;

/**
 * The Driver interface that provides Database driver specific functionality
 * to Schema
 *
 * Created by ranjan on 7/27/15.
 */
public interface Driver {

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

}
