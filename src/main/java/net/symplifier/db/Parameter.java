package net.symplifier.db;

/**
 * The placeholder for values used in the filter condition
 *
 * Created by ranjan on 8/14/15.
 */
public class Parameter<V> implements Query.FilterEntity {

  private final V defaultValue;

  /**
   * Creates a Parameter with a default value
   *
   * @param defaultValue The value to be used for the parameter if a value is
   *                     not provided
   */
  public Parameter(V defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * Get the default value for this parameter
   *
   * @return The default value
   */
  public V getDefault() {
    return defaultValue;
  }
}
