package net.symplifier.db;

import net.symplifier.db.core.field.FieldGenerator;
import net.symplifier.db.query.filter.FilterEntity;

/**
 * Created by ranjan on 7/27/15.
 */
public interface Driver {

  Session createSession();

  String getEntityText(FilterEntity entity);

  <T, F extends Field<T, ?, ?, ?>> FieldGenerator<F> getFieldGenerator(Class<T> type);

  <T, F extends Field<T,?, ?, ?>> FieldGenerator<F> getImmutableFieldGenerator(Class<T> type);

}
