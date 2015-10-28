package net.symplifier.db;

import net.symplifier.core.application.Session;
import org.json.JSONArray;

import java.util.*;

/**
 * The data extraction Query
 *
 * Created by ranjan on 8/13/15.
 */
public interface Query<T extends Model> {

  /**
   * The placeholder for values used in the filter condition
   *
   * Created by ranjan on 8/14/15.
   */
  class Parameter<V> implements Query.FilterEntity {

    private final V defaultValue;
    private Object setter;

    /**
     * Creates a Parameter with a default value
     *
     * @param defaultValue The value to be used for the parameter if a value is
     *                     not provided
     */
    public Parameter(V defaultValue) {
      this.defaultValue = defaultValue;
    }

    public Parameter init(Column<?, V> column) {
      assert(this.setter == null);
      setter = column.getParameterSetter();
      return this;
    }

    public Object getSetter () {
      return setter;
    }

    /**
     * Get the default value for this parameter
     *
     * @return The default value
     */
    public V getDefault() {
      return defaultValue;
    }
  }

  class ParameterList<V> implements Query.FilterEntity {
    private final List<Parameter> parameters = new ArrayList<>();
    private Column<?, V> column;

    @SafeVarargs
    public ParameterList(V ... values) {
      for(V v:values) {
        parameters.add(new Parameter<>(v));
      }
    }

    @SafeVarargs
    public ParameterList(Parameter<V> ... values) {
      Collections.addAll(parameters, values);
    }

    public ParameterList init(Column<?, V> column) {
      assert(this.column == null);
      this.column = column;
      return this;
    }

    public Column<?, V> getColumn() {
      return column;
    }

    public List<Parameter> getParameters() {
      return parameters;
    }
  }

  interface Prepared<T extends Model> {

    <V> Prepared<T> set(Parameter<V> parameter, V value);

    Result<T> execute();
  }

  interface Result<T extends Model> {

    List<T> toList();

    T next();

    JSONArray toJSON();

  }

  interface FilterEntity {

  }

  class Filter<T extends Model> implements FilterEntity {

    private final List<FilterEntity> entities = new ArrayList<>();

    public List<FilterEntity> getEntities() {
      return entities;
    }

    public Filter<T> and(Filter<T> filter) {
      entities.add(FilterOp.and);
      entities.add(filter);
      return this;
    }

    public Filter<T> or(Filter<T> filter) {
      entities.add(FilterOp.or);
      entities.add(filter);
      return this;
    }

    public void append(FilterEntity entity) {
      entities.add(entity);
    }

    /**
     * Counter th number of operations available in the filter. This value is
     * used while generating query to decide if a filter condition needs to
     * be enclosed within parenthesis or not
     *
     * @return The number of {@link net.symplifier.db.Query.FilterOp} that has
     *         been used in the filter
     */
    public int getOperationCount() {
      int count = 0;
      for(FilterEntity e:entities) {
        if (e instanceof FilterOp) {
          count += 1;
        } else if (e instanceof Filter) {
          count += ((Filter) e).getOperationCount();
        }
      }

      return count;
    }
  }

  enum FilterOp implements FilterEntity {
    and,
    or,

    eq,
    notEq,
    lt,
    ltEq,
    gt,
    gtEq,
    in,
    like,

    isNull,
    isNotNull

  }

  /**
   * Keep track of the fields that provide ordering in the Query
   *
   */
  class Order {
    private final Column column;
    private final boolean isDescending;
    public Order(Column column, boolean isDescending) {
      this.column = column;
      this.isDescending = isDescending;
    }

    public Column getColumn() {
      return column;
    }

    public boolean isDescending() {
      return isDescending;
    }
  }


  /**
   * Keep track of the limit values (number of records and offset) for the
   * results in the Query
   */
  class Limit {
    private final Parameter<Integer> limit;
    private final Parameter<Integer> offset;

    public Limit(Parameter<Integer> limit, Parameter<Integer> offset) {
      this.limit = limit;
      this.offset = offset;
    }

    public Parameter<Integer> getLimit() {
      return limit;
    }

    public Parameter<Integer> getOffset() {
      return offset;
    }
  }

  class Builder<T extends Model> {
    private final ModelStructure<T> primaryModel;
    private final Filter<T> filter = new Filter<T>();
    private final Set<Order> orderBy = new LinkedHashSet<>();
    private final List<Join> joins = new ArrayList<>();
    private final Set<Column<T, ?>> fields;

    private Limit limit;

    /**
     * Constructor for use within Model implementation classes to define
     * ready to use Query builder at all locations.
     *
     * <p>Example Declaration:
     * </p><p>
     * <code>public static final Query.Builder&lt;ExampleModel&gt; Query=new Query.Builder&lt;&gt;<>(ExampleModel.class);</code>
     * </p>
     *
     *
     * @param model The model for which the query needs to be built
     */
    public Builder(Class<T> model) {
      this(Schema.get().getModelStructure(model));
    }

    public Builder(ModelStructure<T> modelStructure) {
      this(modelStructure, null);
    }

    public Builder(ModelStructure<T> modelStructure, Column<T, ?>[] columns) {
      this.primaryModel = modelStructure;
      if (columns==null || columns.length == 0) {
        fields = null;
      } else {
        fields = new HashSet<>();
        if (columns[0] != null) {
          Collections.addAll(fields, columns);
        }
      }
    }

    public ModelStructure<T> getPrimaryModel() {
      return primaryModel;
    }

    public List<Join> getJoins() {
      return joins;
    }

    public Filter<T> getFilter() {
      return filter;
    }

    public Set<Order> getOrderBy() {
      if (this.orderBy.size() == 0 && this.getPrimaryModel().getSequenceColumn() !=  null) {
        Set<Order> set = new HashSet<>();
        set.add(new Order(this.getPrimaryModel().getSequenceColumn(), false));
        return set;
      }
      return orderBy;
    }

    public Limit getLimit() {
      return limit;
    }

    public Query<T> build() {
      return primaryModel.getSchema().createQuery(this);
    }



    public Builder<T> where(Filter<T> filter) {
      this.filter.append(filter);
      return this;
    }

    public Builder<T> and(Filter<T> filter) {
      this.filter.append(FilterOp.and);
      this.filter.append(filter);
      return this;
    }

    public Builder<T> or(Filter<T> filter) {
      this.filter.append(FilterOp.or);
      this.filter.append(filter);
      return this;
    }

    public Builder<T> asc(Column<? super T, ?> ... columns) {
      for(Column<? super T, ?> col:columns) {
        this.orderBy.add(new Order(col, false));
      }
      return this;
    }

    public Builder<T> desc(Column<? super T, ?> ... columns) {
      for(Column<? super T, ?> col:columns) {
        this.orderBy.add(new Order(col, true));
      }
      return this;
    }

    public Builder<T> limit(Parameter<Integer> limit, Parameter<Integer> offset) {
      this.limit = new Limit(limit, offset);
      return this;
    }


    public Builder<T> limit(int limit) {
      return limit(new Parameter<>(limit));
    }

    public Builder<T> limit(Parameter<Integer> limit) {
      return limit(limit, null);
    }

    public Builder<T> limit(int limit, Parameter<Integer> offset) {
      return limit(new Parameter<>(limit), offset);
    }

    public Builder<T> limit(Parameter<Integer> limit, int offset) {
      return limit(limit, new Parameter<>(offset));
    }

    public Builder<T> limit(int limit, int offset) {
      return limit(new Parameter<>(limit), new Parameter<>(offset));
    }

//    public <U extends Model, V extends U> Builder<T> join(Class<V> referenceClass, Reference<T, U> reference, Column<V, ?> ... columns) {
//
//      return this;
//    }

    @SafeVarargs
    public final <U extends Model> Builder<T> join(Reference<? super T, U> reference, Column<U, ?> ... columns) {
      joins.add(new Join<>(reference, columns));
      return this;
    }

    @SafeVarargs
    public final <U extends Model> Builder<T> join(Join<U> join, Column<U, ?> ... columns) {
      Collections.addAll(join.fields, columns);
      joins.add(join);
      return this;
    }
  }

  class Join<T extends Model> {
    private final ModelStructure<T> model;
    private final Reference<?, ? super T> reference;
    private final Filter<T> filter;
    private final List<Join> joins = new ArrayList<>();
    private final Set<Column<T, ?>> fields;
    private final Set<Order> orderBy;

    /**
     * Creates a join based on a reference column
     *
     * @param reference The reference column of the model to use for join
     * @param columns The fields of the joining model that needs to be retrieved
     */
    public Join(Reference<?, T> reference, Column<T, ?> ... columns) {
      this(reference.getTargetType(), reference, null, columns);
    }

    /**
     * Creates a join based on a reference column and includes record filter
     * criteria. The filter criteria is not used while joining the models
     * but rather during the record filtering. In other word in SQL the filter
     * is not used in 'ON' but 'WHERE' clause
     *
     * @param reference The reference column of the model to use fo join
     * @param filter The filter criteria for filtering records
     * @param columns
     */

    public Join(Reference<?, T> reference, Filter<T> filter, Column<T, ?> ... columns) {
      this(reference.getTargetType(), reference, filter, columns);
    }

    public Join(ModelStructure<T> model, Reference<?, ? super T> reference, Column<T, ?> ... columns) {
      this(model, reference, null, columns);
    }

    public Join(ModelStructure<T> model, Reference<?, ? super T> reference, Filter<T> filter, Column<T, ?> ... columns) {
      this.model = model;
      this.reference = reference;
      this.filter = filter;
      this.orderBy = new LinkedHashSet<>();

      if(columns.length == 0) {
        fields = null;
      } else {
        fields = new HashSet<>();
        if (columns[0] != null) {
          Collections.addAll(fields, columns);
        }
      }
    }

    public void asc(Column<? super T, ?> ... columns) {
      orderBy(columns, false);
    }

    public void desc(Column<T, ?> ... columns) {
      orderBy(columns, true);
    }

    private void orderBy(Column<? super T, ?>[] columns, boolean desc) {
      for(Column<? super T, ?> column: columns) {
        this.orderBy.add(new Order(column, desc));
      }
    }



    /**
     * Retrieve the model which needs to be joined. In a general case, the model
     * that needs to be joined is the same as given by its Reference Target, but
     * in case of a hierarchical join where a Child model is joined through a
     * parent model's reference, it points to the child model
     *
     * @return The model structure
     */
    public ModelStructure<T> getModel() {
      return model;
    }

    public Reference<?, ? super T> getReference() {
      return reference;
    }

    public List<Join> getJoinChildren() {
      return joins;
    }


    public Set<Order> getOrderBy() {
      if (orderBy.size() == 0 && getModel().getSequenceColumn() != null) {
        HashSet<Order> res = new HashSet<>();
        res.add(new Order(getModel().getSequenceColumn(), false));
        return res;
      }
      return orderBy;
    }

    public Filter<T> filter() {
      return filter;
    }

    public <U extends Model> Join<T> join(Reference<? super T, U> reference) {
      return join(new Join<U>(reference));
    }

    public <U extends Model> Join<T> join(Join<U> join) {
      joins.add(join);
      return this;
    }
  }

  /**
   * Prepares and Executes the query to provide the result. This method uses the
   * default values provided for the parameter of the query
   *
   * @return {@link Result} for retrieving data
   */
  Result<T> execute(DBSession session);

  default Result<T> execute() {
    DBSession session = Session.get(Schema.get(), DBSession.class);
    return execute(session);
  }

  <V> Prepared<T> set(Parameter<V> parameter, V value);

  default Prepared<T> prepare() {
    DBSession session = Session.get(Schema.get(), DBSession.class);
    return prepare(session);
  }

  Prepared<T> prepare(DBSession session);

  String toString();
}
