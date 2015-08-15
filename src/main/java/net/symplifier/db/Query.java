package net.symplifier.db;

import java.sql.Ref;
import java.util.*;

/**
 * Created by ranjan on 8/13/15.
 */
public interface Query<T extends Model> {

  class Parameter<V> {
    private final V defaultValue;

    public Parameter(V defaultValue) {
      this.defaultValue = defaultValue;
    }

    public V getDefault() {
      return defaultValue;
    }
  }

  interface Result<T extends Model> {

  }

  interface FilterEntity {

  }

  class Filter<T extends Model> implements FilterEntity {

    private List<FilterEntity> entities;

    public void add(FilterEntity entity) {
      entities.add(entity);
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

  }

  enum FilterOp implements FilterEntity {
    and,
    or,

  }

  /**
   * Keep track of the fields that provide ordering in the Query
   *
   * @param <T> The primary model
   */
  class Order<T extends Model> {
    private final Column<T, ?> column;
    private final boolean isDescending;
    public Order(Column<T, ?> column, boolean isDescending) {
      this.column = column;
      this.isDescending = isDescending;
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
  }

  class Builder<T extends Model> {
    private final ModelStructure<T> primaryModel;
    private final Filter<T> filter = new Filter<T>();
    private final Map<Column<T, ?>, Order<T>> orderBy = new LinkedHashMap<>();
    private final List<Join> joins = new ArrayList<>();
    private final Set<Column<T, ?>> fields;

    private Limit limit;

    public Builder(ModelStructure<T> modelStructure, Column<T, ?> ... columns) {
      this.primaryModel = modelStructure;
      if (columns.length == 0) {
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

    public Query<T> build() {
      return primaryModel.getSchema().createQuery(this);
    }



    public Builder<T> where(Filter<T> filter) {
      filter.add(filter);
      return this;
    }

    public Builder<T> and(Filter<T> filter) {
      filter.add(FilterOp.and);
      filter.add(filter);
      return this;
    }

    public Builder<T> or(Filter<T> filter) {
      filter.add(FilterOp.or);
      filter.add(filter);
      return this;
    }

    public Builder<T> asc(Column<T, ?> ... columns) {
      for(Column<T, ?> col:columns) {
        this.orderBy.put(col, new Order<T>(col, false));
      }
      return this;
    }

    public Builder<T> desc(Column<T, ?> ... columns) {
      for(Column<T, ?> col:columns) {
        this.orderBy.put(col, new Order<T>(col, true));
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

    public <U extends Model, V extends U> Builder<T> join(Class<V> referenceClass, Reference<T, U> reference, Column<V, ?> ... columns) {

      return this;
    }

    public <U extends Model, V extends U> Builder<T> join(Reference<T, U> reference, Column<V, ?> ... columns) {
      joins.add(new Join<U>(null));
      return this;
    }

    public <U extends Model, V extends U> Builder<T> join(Join<U> join, Column<V, ?> ... columns) {
      joins.add(join);
      return this;
    }
  }

  class Join<T extends Model> {
    private final Reference<?, T> reference;
    private final Filter<T> filter;
    private final List<Join> joins = new ArrayList<>();
    private final Set<Column<T, ?>> fields;

    public Join(Reference<?, T> reference, Column<T, ?> ... columns) {
      this(reference, null, columns);
    }

    public Join(Reference<?, T> reference, Filter<T> filter, Column<T, ?> ... columns) {
      this.reference = reference;
      this.filter = filter;

      if (columns.length == 0) {
        fields = null;
      } else {
        fields = new HashSet<>();
        if (columns[0] != null) {
          Collections.addAll(fields, columns);
        }
      }
    }

    public Reference<?, T> getReference() {
      return reference;
    }

    public Filter<T> getFilter() {
      return filter;
    }

    public <U extends Model> Join<T> join(Reference<T, U> reference) {
      return join(new Join<U>(reference));
    }

    public <U extends Model> Join<T> join(Join<U> join) {
      joins.add(join);
      return this;
    }



  }

  Query<T> set(Parameter<String> parameter, String value);

  Query<T> set(Parameter<Integer> parameter, Integer value);

  Query<T> set(Parameter<Double> parameter, Double value);

  Query<T> set(Parameter<Byte> parameter, Byte value);

  Query<T> set(Parameter<Long> parameter, Long value);

  Query<T> set(Parameter<Float> parameter, Float value);

  Query<T> set(Parameter<Short> parameter, Short value);

  Query<T> set(Parameter<byte[]> parameter, byte[] value);

  Result<T> execute();

}
