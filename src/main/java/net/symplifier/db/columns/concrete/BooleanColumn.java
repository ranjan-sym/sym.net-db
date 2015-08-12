package net.symplifier.db.columns.concrete;

import net.symplifier.db.Model;
import net.symplifier.db.columns.Column;
import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 8/9/15.
 */
public class BooleanColumn<M extends Model> extends Column<M, Boolean> {

  public BooleanColumn(String name, Class<M> modelType) {
    super(name, modelType);
  }

  @Override
  public Class<Boolean> getType() {
    return Boolean.class;
  }

  @Override
  public void buildQuery(Query query, StringBuilder queryString) {

  }
}
