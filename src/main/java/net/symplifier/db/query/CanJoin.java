package net.symplifier.db.query;

import net.symplifier.db.Model;
import net.symplifier.db.query.filter.Filter;

/**
 * Created by ranjan on 7/30/15.
 */
public interface CanJoin<M extends Model, T extends Model> {

  Join<M, T> on(Filter<M> filter);

  Join<M, T> join();


}
