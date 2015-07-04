package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public class Condition<T> implements FilterEntity {
  public final static int IS = 1;
  public final static int GREATER_THAN = 2;
  public final static int LESS_THAN = 3;
  public final static int GREATER_THAN_OR_EQUALS = 4;
  public final static int LESS_THAN_OR_EQUALS = 5;
  public final static int IS_NOT = 6;
  public final static int LIKE = 7;

  private final Column column;
  private final int operator;

  private T value;


  public Condition(Column column, int operator, T value) {
    this.column = column;
    this.operator = operator;
    this.value = value;
  }

  public Column getColumn() {
    return column;
  }

  public int getOperator() {
    return operator;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

}
