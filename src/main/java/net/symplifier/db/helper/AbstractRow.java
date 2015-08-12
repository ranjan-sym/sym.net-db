package net.symplifier.db.helper;

import net.symplifier.db.columns.Column;
import net.symplifier.db.Row;
import net.symplifier.db.Schema;

/**
 * Created by ranjan on 7/27/15.
 */
public class AbstractRow implements Row {
  private final Schema schema;
  private long id;

  public <T> T get(Column<?, T> column) {
    return null;
  }

  public <T> void set(Column<?, T> column, T value) {

  }

  protected AbstractRow(Schema schema) {
    this.schema = schema;
  }



  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setId(long id) {
    this.id = id;
  }

  @Override
  public boolean isNew() {
    return false;
  }

  @Override
  public boolean isModified() {
    return false;

  }
}
