package net.symplifier.db.query.filter;

import net.symplifier.db.Model;
import net.symplifier.db.query.Alias;

/**
 * Created by ranjan on 7/30/15.
 */
public interface FilterOwner<M extends Model> {
  Alias<M> getAlias();
}
