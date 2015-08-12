package net.symplifier.db.query;

import net.symplifier.db.columns.Column;
import net.symplifier.db.Model;
import net.symplifier.db.Schema;
import net.symplifier.db.query.filter.Filter;
import net.symplifier.db.query.filter.FilterOwner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ranjan on 7/30/15.
 */
public class QueryBuilder<M extends Model> implements HasJoin<M>, FilterOwner<M> {
  private final Alias<M> alias = new Alias<>();
  private final Schema schema;
  private final Class<M> modelClass;
  private Filter<M> filter;
  private Joins<M> joins = new Joins<>(this);
  private List<Order> orders = new ArrayList<>();

  private int page = 0;
  private int pageLimit = 100;

  public QueryBuilder(Schema schema, Class<M> modelClass) {
    assert(schema != null);
    assert(modelClass != null);

    this.schema = schema;
    this.modelClass = modelClass;
  }

  public Schema getSchema() {
    return schema;
  }

  public Class<M> getModelClass() {
    return modelClass;
  }

  public Alias<M> getAlias() {
    return alias;
  }

  public Joins<M> getJoins() {
    return joins;
  }

  public QueryBuilder<M> where(Filter<M> filter) {
    this.filter = filter;
    this.filter.setOwner(this);
    return this;
  }

  public <T extends Model> QueryBuilder<M> join(CanJoin<M, T> reference) {
    joins.add(reference.join());
    return this;
  }

  public <T extends Model> QueryBuilder<M> join(Join<M, T> join) {
    joins.add(join);
    return this;
  }

  public <T extends Model> QueryBuilder<M> asc(Alias<T> alias, Column<T, ?> column) {
    orders.add(new Order<T>(alias, column, false));
    return this;
  }

  public <T extends Model> QueryBuilder<M> desc(Alias<T> alias, Column<T, ?> column) {
    orders.add(new Order<T>(alias, column, true));
    return this;
  }

  public QueryBuilder<M> limit(int page, int pageSize) {
    return this;
  }

  public Query<M> build() {
    return schema.createQuery(this);
  }

}
