package net.symplifier.db.query;

import net.symplifier.db.Model;
import net.symplifier.db.Reference;
import net.symplifier.db.query.filter.Filter;
import net.symplifier.db.query.filter.FilterOwner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ranjan on 7/30/15.
 */
public class Join<S extends Model, M extends Model>
        implements HasJoin<M>, FilterOwner<M> {
  private HasJoin<S> parent;
  //private final Reference<S, M> reference;
  //private final Filter<M> filter;
  private final Alias<M> alias = new Alias<M>();

  private final Joins<M> joins = new Joins<>(this);

//  public Join(Reference<S, M> reference) {
//    this(reference, null);
//  }
//
//  public Join(Reference<S, M> reference, Filter<M> filter) {
//    this.reference = reference;
//    this.filter = filter;
//    this.filter.setOwner(this);
//  }

  void setParent(HasJoin<S> parent) {
    this.parent = parent;
  }

//  public Filter<M> getFilter() {
//    return filter;
//  }

//  public Reference<S, M> getReference() {
//    return reference;
//  }

  @Override
  public Alias<M> getAlias() {
    return alias;
  }

  public HasJoin<S> getParent() {
    return parent;
  }

  @Override
  public <T extends Model> HasJoin<M> join(CanJoin<M, T> reference) {
    join(reference.join());
    return this;
  }

  @Override
  public <T extends Model> HasJoin<M> join(Join<M, T> join) {
    joins.add(join);
    return this;
  }

  @Override
  public Joins<M> getJoins() {
    return joins;
  }
}
