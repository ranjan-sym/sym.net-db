package net.symplifier.db;

/**
 * Created by ranjan on 7/28/15.
 */
public class ModelInstance implements Model {
  private final ModelStructure structure;

  @Override
  public ModelStructure getStructure() {
    return structure;
  }

  @Override
  public <T> T get(Column<?, T> column, int level) {
    return (T)set.rows[level].get(column.getIndex());
  }

  public <T> void set(Column<?, T> column, int level, T value) {
    ModelRow row = set.rows[level];

    int colIndex = column.getIndex();
    Object chk = row.get(colIndex);

    // Let's first see if its actually a change
    if (value == chk || (value != null && value.equals(chk))) {
      return;
    }

    if (!row.isModified()) {
      row = set.rows[level] = row.copy();
    }

    row.set(colIndex, value, true);
  }

  public boolean isModified() {
    for(ModelRow r:set.rows) {
      if(r.isModified()) {
        return true;
      }
    }
    return false;
  }

  public class ModelSet {
    private ModelRow[] rows;
  }

  private ModelSet set;

  public ModelInstance(ModelStructure structure) {
    this.structure = structure;
  }





}
