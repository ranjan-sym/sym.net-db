package net.symplifier.db.filter;

/**
 * Created by ranjan on 7/29/15.
 */
public class Parameter<T> implements Entity {
  private T value;
  public Parameter(T value) {
    this.value = value;
  }

  public Parameter() {

  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }
}
