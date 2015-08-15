package net.symplifier.db;

/**
 * The underlying data class that actually keeps all the model data. A ModelInstance
 * provides a layer on top of this
 *
 * Created by ranjan on 8/12/15.
 */
public class ModelRow<T extends Model> {

  private final ModelStructure<T> owner;

  private Long id;
  private final Object[] rowData;

  // Keep track of the fields that have been modified;
  private long modificationBits;

  ModelRow(ModelStructure owner) {
    this.owner = owner;
    this.id = null;
    this.rowData = new Object[owner.getColumnCount()];
    this.modificationBits = 0;
  }

  ModelRow(ModelRow ref) {
    this.owner = ref.owner;
    this.id = ref.id;
    this.rowData = new Object[ref.rowData.length];
    System.arraycopy(ref.rowData, 0, this.rowData, 0, rowData.length);
    this.modificationBits = 0;
  }

  public ModelRow copy() {
    return new ModelRow(this);
  }


  public Long getId() {
    return id;
  }

  public boolean isModified() {
    return id==null || modificationBits != 0;
  }

  public Object get(int index) {
    return rowData[index];
  }

  public void set(int index, Object obj) {
    set(index, obj, false);
  }

  public void set(int index, Object obj, boolean makeDirty) {
    rowData[index] = obj;
    this.modificationBits |= (1L << index);
  }

}
