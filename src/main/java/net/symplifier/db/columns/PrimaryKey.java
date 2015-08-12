package net.symplifier.db.columns;

import net.symplifier.db.Model;
import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 7/31/15.
 */
public class PrimaryKey<M extends Model> extends Column<M, Long> {
  public PrimaryKey(String name, Class<M> modelType) {
    super(name, modelType);
  }

  @Override
  public Class<Long> getType() {
    return Long.class;
  }

  @Override
  public void buildQuery(Query query, StringBuilder res) {

  }
}
