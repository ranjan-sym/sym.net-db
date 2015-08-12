package net.symplifier.db.columns.concrete;

import net.symplifier.db.Model;
import net.symplifier.db.columns.Column;
import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 7/31/15.
 */
public class StringColumn<M extends Model> extends Column<M, String> {

  public StringColumn(String name, Class<M> modelType) {
    super(name, modelType);
  }

  @Override
  public Class<String> getType() {
    return String.class;
  }

  @Override
  public void buildQuery(Query query, StringBuilder res) {

  }
}
