package net.symplifier.db;

/**
 * An interface for declaring the interface model in database. A table that works
 * like an interface in OOP.
 *
 *
 * Created by ranjan on 8/13/15.
 */
public interface ModelInterface extends Model {

  /**
   * Retrieve the value of the field from the interface table. The value
   * retrieval from an interface based table is a little bit tricky as the
   * same column would be used in a number of different tables that implement
   * this interface. The method provides a dynamic way to retrieve the value of
   * the interface column from the implementing model
   *
   * @param column The column for which the values needs to be retrieved
   * @param <T> The type of the value that this column maps to
   * @return returns the value of the field
   */
  default<T> T getImplementedValue(Column<?, T> column) {
    return get(column, column.getLevel(getStructure()));
  }

  /**
   * Sets the value of the field in the interface table based on the model
   * that implements this interface
   *
   * @param column The column for which the value needs to be set
   * @param value The field value that has to be set
   * @param <T> The type of the value
   */
  default<T> void setImplementedValue(Column<?, T> column, T value) {
    set(column, column.getLevel(getStructure()), value);
  }


}
