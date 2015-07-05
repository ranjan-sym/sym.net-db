package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public abstract class Query<T extends Row> {

  protected final Model<T> primaryModel;
  protected final Driver driver;

  protected Filter<T> filter = null;

  public Query(Driver driver, Model<T> primaryModel) {
    this.driver = driver;
    this.primaryModel = primaryModel;
  }

  public abstract void reset();

  public Model<T> getModel() {
    return primaryModel;
  }

  public Filter<T> getFilter() {
    // Returning a null from this function can be a problem.
    // So just adding a TRUE entity and returning a new
    // filter object
    if (filter == null) {
      filter = new Filter<>(this, primaryModel, null, Filter.TRUE);
    }
    return filter;
  }

  public Filter<T> filter(Condition<?> condition) {
    reset();
    filter = new Filter<>(this, primaryModel, null, condition);
    return filter;
  }

  public abstract Query<T> clearLimit();

  public abstract Query<T> limit(int page, int pageSize);

  public abstract Query<T> clearOrderBy();

  public final Query<T> orderBy(Column column) {
    return orderBy(column, false);
  }

  public abstract Query<T> orderBy(Column column, boolean desc);

  protected abstract Query<T> createCopy();

  public abstract RowIterator<T> getRows() throws DatabaseException;

  public abstract int getSize() throws DatabaseException;

  public final Query<T> copy() {
    Query<T> q =  createCopy();
    q.filter = this.filter;
    return q;
  }
}
