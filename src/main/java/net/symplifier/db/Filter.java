package net.symplifier.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public class Filter<T extends Row> implements FilterEntity {

  public static final FilterEntity AND = new FilterEntity() {};
  public static final FilterEntity OR = new FilterEntity() {};
  public static final FilterEntity NOT = new FilterEntity() {};

  private final Model<T> primaryModel;
  private final Filter<T> parent;
  private final Query<T> owner;

  private final LinkedList<FilterEntity> entities = new LinkedList<>();

  public Iterator<FilterEntity> getEntities() {
    return entities.iterator();
  }

  public Filter(Query<T> owner, Model<T> primaryModel, Filter<T> parent, Condition condition) {
    this.owner = owner;
    this.primaryModel = primaryModel;
    this.parent = parent;

    entities.add(condition);
  }

  public Query<T> query() {
    return owner;
  }

  public Filter<T> and(Condition condition) {
    entities.add(AND);
    entities.add(condition);
    return this;
  }

  public Filter<T> or(Condition condition) {
    entities.add(OR);
    entities.add(condition);
    return this;
  }


  public Filter<T> andBlock(Condition condition) {
    entities.add(AND);
    Filter<T> block = new Filter<>(this.owner, primaryModel, this, condition);
    entities.add(block);
    return block;

  }

  public Filter<T> orBlock(Condition condition) {
    entities.add(OR);
    Filter<T> block = new Filter<>(this.owner, primaryModel, this, condition);
    entities.add(block);
    return block;
  }

  public Filter<T> endBlock() {
    return parent;
  }

  public Filter<T> not() {
    entities.addFirst(NOT);
    return parent;
  }


}
