package net.symplifier.db;

/**
 * The underlying data class that actually keeps all the model data. A ModelInstance
 * provides a layer on top of this
 *
 * Created by ranjan on 8/12/15.
 */
public class ModelRow {
  // The model's structure information to which this row belongs
  private final ModelStructure owner;

  // The first data on this array should be primary key
  private final Object[] rowData;

  // Keep track of the fields that have been modified;
  private long modificationBits;

  /**
   * A new model row creation
   *
   * @param owner The model's structure information
   */
  ModelRow(ModelStructure owner) {
    this.owner = owner;
    this.rowData = new Object[owner.getColumnCount()];
    this.modificationBits = 0;
  }

  public Object[] getData() {
    return rowData;
  }

  /**
   * A model created as a copy of another row. Used for making the model
   * somewhat immutable
   *
   * @param ref The source row
   */
  ModelRow(ModelRow ref) {
    this.owner = ref.owner;
    this.rowData = new Object[ref.rowData.length];
    System.arraycopy(ref.rowData, 0, this.rowData, 0, rowData.length);
    this.modificationBits = 0;
  }

  /**
   * Retrieve the model structure information
   *
   * @return {@link ModelStructure}
   */
  public ModelStructure getStructure() {
    return owner;
  }

  /**
   * Create a copy of this Row
   *
   * @return A new {@link ModelRow} that has the same data
   */
  public ModelRow copy() {
    return new ModelRow(this);
  }

  /**
   * Retrieve the primary key id of this row
   *
   * @return long the primary key id
   */
  public Long getId() {
    return (Long)rowData[0];
  }

  /**
   * Check if the row is modified or not. A row is considered to be modified
   * if it doesn't have a primary key (never been saved) or some of the fields
   * have been modified
   *
   * @return {@code true} if there are columns to be saved otherwise null
   */
  public boolean isModified() {
    return getId()==null || modificationBits != 0;
  }

  /**
   * Checks if the individual fields within the row is modified or not.
   *
   * @param item The 0 based index of the field that needs to be checked for
   *             modification
   * @return {@code true} if the field has been modified
   */
  public boolean isModified(int item) {
    return (modificationBits & (1L << item)) != 0L;
  }

  /**
   * Clears the modification flag set on individual fields of this row. This
   * method must be called after a database save or after a record is loaded
   * from database.
   */
  public void clearFlag() {
    this.modificationBits = 0;
  }

  /**
   * Retrieve the field value of the row at the given index
   *
   * @param index The 0 based index of the field whose value is to be retrieved
   * @return The field value
   */
  public Object get(int index) {
    return rowData[index];
  }

  /**
   * Set the field value of the row and marks the changes at dirty. This method
   * first checks if the value being set is different than the existing value or
   * not. The modification flag is set only if the values are different.
   *
   * @param index The 0 based index of the field whose value is to be changed
   * @param obj The value to be set
   */
  public void set(int index, Object obj) {
    set(index, obj, true);
  }

  /**
   * Set the field value of the row with an option to mark changes made. This
   * method is invoked when populating the row with the data from the database
   * and we don't want to modify the modification bits
   *
   * @param index The 0 based index of the field whose value is to be changed
   * @param obj The value to be set
   * @param makeDirty flag to change the modification flag for this bit
   */
  public void set(int index, Object obj, boolean makeDirty) {
    if ((obj == null && rowData[index] != null) ||
            (obj != null && !obj.equals(rowData[index]))) {
      rowData[index] = obj;
      if (makeDirty) {
        this.modificationBits |= (1L << index);
      }
    }
  }
}
