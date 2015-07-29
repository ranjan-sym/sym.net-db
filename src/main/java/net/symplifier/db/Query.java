package net.symplifier.db;

import net.symplifier.db.filter.Filter;
import net.symplifier.db.filter.UnaryOperator;

/**
 * Created by ranjan on 7/28/15.
 */
public interface Query<M extends Model> {

  boolean isNormalized();

  Schema getSchema();

  void execute();

  boolean next();


  Integer getInt(Column<M, Integer> column);

  byte[] getBlob(Column<M, byte[]> column);

  Query<M> where(Filter<M> filter);

  <T extends Model> Query<M> join(Reference<M, T> referenceColumn);

  <T extends Model> Query<M> join(Many<M, T> references);
}
