package net.symplifier.db.columns.concrete;

import net.symplifier.db.Model;
import net.symplifier.db.columns.Column;
import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 7/31/15.
 */
public class IntegerColumn<M extends Model> extends Column<M, Integer> {

  public IntegerColumn(String name, Class<M> modelType) {
    super(name, modelType);
  }

  public final Class<Integer> getType() { return Integer.class; };

  @Override
  public void buildQuery(Query query, StringBuilder queryString) {

  }

}
