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

  public Condition<T> is(T value) {
    return new Condition<>(this, Condition.IS, value);
  }

  public Condition<T> greaterThan(T value) {
    return new Condition<>(this, Condition.GREATER_THAN, value);
  }

  public Condition<T> lessThan(T value) {
    return new Condition<>(this, Condition.LESS_THAN, value);
  }

  public Condition<T> greaterThanOrEquals(T value) {
    return new Condition<>(this, Condition.GREATER_THAN_OR_EQUALS, value);
  }

  public Condition<T> lessThanOrEquals(T value) {
    return new Condition<>(this, Condition.LESS_THAN_OR_EQUALS, value);
  }

  public Condition<T> isNot(T value) {
    return new Condition<>(this, Condition.IS_NOT, value);
  }

  public Condition<T> like(T value) {
    return new Condition<>(this, Condition.LIKE, value);
  }


}
