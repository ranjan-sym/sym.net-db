package net.symplifier.db;

/**
 * The underlying data class that actually keeps all the model data. A ModelInstance
 * provides a layer on top of this
 *
 * Created by ranjan on 8/12/15.
 */
public class ModelRow<T extends Model> {

  private final ModelStructure owner;

  // The first data on this array should be primary key
  private final Object[] rowData;

  // Keep track of the fields that have been modified;
  private long modificationBits;

  ModelRow(ModelStructure owner) {
    this.owner = owner;
    this.rowData = new Object[owner.getColumnCount()];
    this.modificationBits = 0;
  }

  ModelRow(ModelRow ref) {
    this.owner = ref.owner;
    this.rowData = new Object[ref.rowData.length];
    System.arraycopy(ref.rowData, 0, this.rowData, 0, rowData.length);
    this.modificationBits = 0;
  }

  public ModelRow copy() {
    return new ModelRow(this);
  }

  public Long getId() {
    return (Long)rowData[0];
  }

  public boolean isModified() {
    return getId()==null || modificationBits != 0;
  }

  public void clearFlag() {
    this.modificationBits = 0;
  }

  public Object get(int index) {
    return rowData[index];
  }

  public void set(int index, Object obj) {
    set(index, obj, false);
  }

  public void set(int index, Object obj, boolean makeDirty) {
    rowData[index] = obj;
    if (makeDirty) {
      this.modificationBits |= (1L << index);
    }
  }

}
