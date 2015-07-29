package net.symplifier.db;

import net.symplifier.db.filter.Filter;

/**
 * Created by ranjan on 7/30/15.
 */
public class Reference<M extends Model, T extends Model> {
  <V> Filter<T> on(Column<T, V> column) {
    return new Filter<T>();
  }
}
