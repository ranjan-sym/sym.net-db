package net.symplifier.db;

import net.symplifier.db.filter.*;

import javax.management.relation.Relation;

/**
 * Created by ranjan on 7/27/15.
 */
public class Column<M extends Model, T> implements Entity {

  private final String name;
  private final Class<M> modelType;
  private final Class<T> type;

  private Query<M> findQuery;
  private Parameter<T> findParameter;

  public Column(String name, Class<T> type, Class<M> modelType) {
    this.name = name;
    this.type = type;
    this.modelType = modelType;
  }

  public String getName() {
    return name;
  }

  public Class getType() {
    return type;
  }

  private Filter<M> relation(RelationalOperator operator) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(operator);
    return filter;
  }

  private Filter<M> relation(RelationalOperator operator, Entity param) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(operator);
    filter.append(param);
    return filter;
  }

  public Filter<M> is(T value) {
    return relation(RelationalOperator.eq, new Parameter<>(value));
  }

  public Filter<M> is(Parameter<T> param) {
    return relation(RelationalOperator.eq, param);
  }

  public Filter<M> isNot(T value) {
    return relation(RelationalOperator.notEq, new Parameter<>(value));
  }

  public Filter<M> isNull() {
    return relation(RelationalOperator.isNull);
  }

  public Filter<M> notNull() {
    return relation(RelationalOperator.notNull);
  }

  public Filter<M> like(String pattern) {
    return relation(RelationalOperator.like, new Pattern(pattern));
  }

  public Filter<M> lessThan(T value) {
    return relation(RelationalOperator.lessThan, new Parameter<>(value));
  }

  public Filter<M> lessThanEqual(T value) {
    return relation(RelationalOperator.lessThenEq, new Parameter<>(value));
  }

  public Filter<M> greaterThan(T value) {
    return relation(RelationalOperator.greaterThan, new Parameter<>(value));
  }

  public Filter<M> greaterThanEqual(T value) {
    return relation(RelationalOperator.greaterThanEq, new Parameter<>(value));
  }

  public Filter<M> between(T r1, T r2) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(RelationalOperator.greaterThanEq);
    filter.append(new Parameter<>(r1));
    filter.append(LogicalOperator.AND);
    filter.append(RelationalOperator.lessThan);
    filter.append(new Parameter<>(r2));
    return filter;
  }

  public Filter<M> between(Parameter<T> p1, Parameter<T> p2) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(RelationalOperator.greaterThanEq);
    filter.append(p1);
    filter.append(LogicalOperator.AND);
    filter.append(RelationalOperator.lessThan);
    filter.append(p2);
    return filter;
  }

  public synchronized M find(Schema schema, T value, boolean normalized) {
    if (findQuery == null || findQuery.isNormalized() != normalized || findQuery.getSchema() != schema) {
      findParameter = new Parameter<>(value);
      findQuery = schema.query(modelType, normalized).where(this.is(findParameter));
    } else {
      findParameter.setValue(value);
    }

    findQuery.execute();
    if(findQuery.next()) {
      // create an instance of the model, update and return it here
      M model = schema.createModel(modelType);
      
      return model;
    } else {
      return null;
    }
  }

}
