package net.symplifier.tests.db.system;

import net.symplifier.db.*;

/**
 * Created by ranjan on 8/20/15.
 */
public class Publisher extends ModelInstance<Publisher> {

  public static Query.Builder<Publisher> Q() { return new Query.Builder<>(Publisher.class); }

  public static final Column.Primary<Publisher> id = new Column.Primary<>();
  public static final Column.Text<Publisher> name = new Column.Text<>();

  public static final Relation.HasMany<Publisher, Book> books = new Relation.HasMany<>(Book.class, Book.publisher);

  public String getName() {
    return get(name);
  }

  public void setName(String value) {
    set(name, value);
  }


}
