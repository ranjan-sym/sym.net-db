package net.symplifier.db.query;

import net.symplifier.db.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ranjan on 7/30/15.
 */
public class Joins<S extends Model> implements Iterable<Join<S, ? extends Model>> {
  private final HasJoin<S> parent;
  List<Join<S, ? extends Model>> joins = new ArrayList<>();

  public Joins(HasJoin<S> parent) {
    this.parent = parent;
  }

  public void add(Join<S, ? extends Model> join) {
    joins.add(join);
    join.setParent(parent);
  }

  @Override
  public Iterator<Join<S, ? extends Model>> iterator() {
    return joins.iterator();
  }
}
