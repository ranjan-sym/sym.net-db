package net.symplifier.db.columns.concrete;

import net.symplifier.db.Model;
import net.symplifier.db.columns.Column;
import net.symplifier.db.query.Query;

import java.util.Date;

/**
 * Created by ranjan on 8/9/15.
 */
public class DateColumn<M extends Model> extends Column<M, Date> {
  public DateColumn(String name, Class<M> modelType) {
    super(name, modelType);
  }

  @Override
  public Class<Date> getType() {
    return Date.class;
  }

  @Override
  public void buildQuery(Query query, StringBuilder queryString) {

  }
}
