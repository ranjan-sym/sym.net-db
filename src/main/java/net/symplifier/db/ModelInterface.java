package net.symplifier.db;

/**
 * An interface for declaring the interface model in database. A table that works
 * like an interface in OOP.
 *
 *
 * Created by ranjan on 8/13/15.
 */
public interface ModelInterface extends Model {

  default<T> T getImplementedValue(Column<?, T> column) {
    return get(column, column.getLevel(getStructure()));
  }

  default<T> void setImplementedValue(Column<?, T> column, T value) {
    set(column, column.getLevel(getStructure()), value);
  }


}
