package net.symplifier.db.query;

import net.symplifier.db.Model;
import net.symplifier.db.Reference;

/**
 * Created by ranjan on 7/30/15.
 */
public interface HasJoin<M extends Model> {

  <T extends Model> HasJoin<M> join(CanJoin<M, T> reference);

  <T extends Model> HasJoin<M> join(Join<M, T> join);

  Joins<M> getJoins();

  Alias<M> getAlias();
}
