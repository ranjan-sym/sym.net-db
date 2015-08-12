package net.symplifier.db.columns.concrete;

import net.symplifier.db.Model;
import net.symplifier.db.columns.Column;
import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 7/31/15.
 */
public class DoubleColumn<M extends Model> extends Column<M, Double> {

  public DoubleColumn(String name, Class<M> modelType) {
    super(name, modelType);
  }

  @Override
  public Class<Double> getType() {
    return Double.class;
  }

  @Override
  public void buildQuery(Query query, StringBuilder res) {

  }
}
