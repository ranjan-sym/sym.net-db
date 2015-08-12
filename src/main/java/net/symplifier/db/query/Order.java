package net.symplifier.db.query;

import net.symplifier.db.columns.Column;
import net.symplifier.db.Model;

/**
 * Created by ranjan on 7/30/15.
 */
public class Order<M extends Model> {
  private final Alias<M> alias;
  private final Column<M, ?> column;
  private final boolean descending;


  public Order(Alias<M> alias, Column<M, ?> column, boolean descending) {
    this.alias = alias;
    this.column = column;
    this.descending = descending;
  }

  public Alias<M> getAlias() {
    return alias;
  }

  public Column<M, ?> getColumn() {
    return column;
  }

  public boolean isDescending() {
    return descending;
  }
}
