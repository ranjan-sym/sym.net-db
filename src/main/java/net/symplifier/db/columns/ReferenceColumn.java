package net.symplifier.db.columns;

import net.symplifier.db.Model;
import net.symplifier.db.core.field.GenericSetter;
import net.symplifier.db.query.Query;

import java.util.LongSummaryStatistics;

/**
 * Created by ranjan on 7/31/15.
 */
public class ReferenceColumn<M extends Model, T extends Model> extends Column<M, Long> {

  public ReferenceColumn(String name, Class<M> modelType) {
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
