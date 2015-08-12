package net.symplifier.db.query.filter;

import net.symplifier.db.Model;
import net.symplifier.db.query.Alias;
import net.symplifier.db.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * The Filter class to handle the filter criteria in the WHERE clause
 * as well the the JOINs
 *
 * The Filter has to implement FilterOwner for the invert case as the
 * owner of the current filter is not set when the invert function is
 * invoked, so the current filter becomes the owner of the invert
 * filter
 *
 *
 * Created by ranjan on 7/28/15.
 */
public class Filter<M extends Model> implements FilterEntity, FilterOwner<M> {

  private FilterOwner<M> owner;

  private List<FilterEntity> entities = new ArrayList<>();

  public Filter() {

  }

  public void setOwner(FilterOwner<M> owner) {
    this.owner = owner;
  }

  public Filter<M> and(Filter<M> filter) {
    entities.add(Operator.and);
    entities.add(filter);

    return this;
  }

  public String getQueryText() {
    throw new UnsupportedOperationException();

  }

  public void buildQuery(Query query, StringBuilder str) {
    query.buildFilter(str, owner.getAlias(), this);
  }

  public Filter<M> or(Filter<M> filter) {
    entities.add(Operator.or);
    entities.add(filter);

    return this;
  }

  public Filter<M> invert() {
    Filter<M> filter = new Filter<>();
    filter.setOwner(this);
    filter.entities.add(Operator.not);
    filter.entities.add(this);
    return filter;
  }

  public void append(FilterEntity entity) {
    entities.add(entity);
  }


  public List<FilterEntity> getEntities() {
    return entities;
  }

  @Override
  public Alias<M> getAlias() {
    return owner.getAlias();
  }

}
