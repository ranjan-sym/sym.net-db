package net.symplifier.db.query;

import net.symplifier.db.*;
import net.symplifier.db.columns.Column;
import net.symplifier.db.query.filter.Filter;
import net.symplifier.db.query.filter.FilterParameter;

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

  <T extends Model> void buildFilter(StringBuilder str, Alias<T> alias, Filter<T> mFilter);

  <P> void appendParameter(FilterParameter<P> parameter);

  void update(Model model);

}
