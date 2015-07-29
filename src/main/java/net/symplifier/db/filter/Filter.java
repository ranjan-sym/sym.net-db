package net.symplifier.db.filter;

import net.symplifier.db.Column;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ranjan on 7/28/15.
 */
public class Filter<M> implements Entity {


  private List<Entity> entities = new ArrayList<Entity>();

  public Filter<M> and(Filter<M> filter) {
    entities.add(LogicalOperator.AND);
    entities.add(filter);

    return this;
  }

  public Filter<M> or(Filter<M> filter) {
    entities.add(LogicalOperator.OR);
    entities.add(filter);

    return this;
  }

  public Filter<M> invert() {
    Filter<M> filter = new Filter<>();
    filter.entities.add(UnaryOperator.NOT);
    filter.entities.add(this);
    return filter;
  }

  public void append(Entity entity) {
    entities.add(entity);
  }
}
