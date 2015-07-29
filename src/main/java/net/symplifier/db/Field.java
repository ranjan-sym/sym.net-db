package net.symplifier.db;

/**
 * Created by ranjan on 7/27/15.
 */
public interface Field<T> {

  void update(Column column, Query query);
}
