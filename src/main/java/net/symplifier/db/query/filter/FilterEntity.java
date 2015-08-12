package net.symplifier.db.query.filter;

import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 7/29/15.
 */
public interface FilterEntity {
  /**
   * The Filter Entity provides a mechanism to help build the query, in some
   * cases the entity might only add up to the string query component while
   * in other cases, it might be more complicated and might need adding
   * @param query The query which is being built
   * @param queryString The string component of the query
   */
  void buildQuery(Query query, StringBuilder queryString);
}
