package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public final class Column <T> {
  final Class<T> type;
  final String name;
  final int size;
  final boolean required;
  

  public Column(String name, Class<T> type) {
    this(name, type, 0, false);

  }
  public Column(String name, Class<T> type, int size, boolean required) {
    this.name = name;
    this.type = type;
    this.size = size;
    this.required = required;
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  public boolean isRequired() {
    return required;
  }

  public Condition is(T value) {
    return new Condition<T>(this, Condition.IS, value);
  }

  public Condition greaterThan(T value) {
    return new Condition<T>(this, Condition.GREATER_THAN, value);
  }

  public Condition lessThan(T value) {
    return new Condition<T>(this, Condition.LESS_THAN, value);
  }

  public Condition greaterThanOrEquals(T value) {
    return new Condition<T>(this, Condition.GREATER_THAN_OR_EQUALS, value);
  }

  public Condition lessThanOrEquals(T value) {
    return new Condition<T>(this, Condition.LESS_THAN_OR_EQUALS, value);
  }

  public Condition isNot(T value) {
    return new Condition<T>(this, Condition.IS_NOT, value);
  }

  public Condition like(T value) {
    return new Condition<T>(this, Condition.LIKE, value);
  }


}
