package net.symplifier.db;

import net.symplifier.db.columns.Column;
import net.symplifier.db.core.field.GenericGetter;
import net.symplifier.db.core.field.GenericSetter;
import net.symplifier.db.core.field.Getter;
import net.symplifier.db.core.field.Setter;
import net.symplifier.db.query.Query;

/**
 *
 * A field holds the individual column value for each and every record. Each
 * driver will provide a concrete field instance that would provide a mechanism
 * to move data to and from underlying database system.
 *
 * @param <T> The type of the field
 * @param <P> The type of the reference used by Getter, Setter for updating
 *            the field
 * @param <G> The type of the Getter used for getting data from database
 * @param <S> The type of the Setter used for setting data into database
 */
public interface Field<T, P, G extends Getter<P>, S extends Setter<P>> {

  void apply(G getter, P reference);

  void apply(S setter, P reference);

  T get();

  void set(T value);

}
