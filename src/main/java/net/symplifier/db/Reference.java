package net.symplifier.db;

import net.symplifier.db.columns.Column;
import net.symplifier.db.query.Join;
import net.symplifier.db.query.filter.Filter;

/**
 * Created by ranjan on 7/30/15.
 */
public class Reference<M extends Model> {
  private final Field<Long, ?, ?, ?> field;

  public Reference(Field<Long, ?, ?, ?> field) {
    this.field = field;
  }

  public M get() {
    return null;
  }

  public void set(M value) {

  }
}
