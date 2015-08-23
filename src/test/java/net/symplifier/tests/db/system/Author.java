package net.symplifier.tests.db.system;

import net.symplifier.db.*;

/**
 * Created by ranjan on 8/20/15.
 */
public class Author extends ModelInstance<Author> {

  public static Query.Builder<Author> Q() { return new Query.Builder<>(Author.class); }


  public static final Column.Primary<Author> id = new Column.Primary<>();
  public static final Column.Text<Author> name = new Column.Text<>();

  public static final Relation.HasMany<Author, Book> books = new Relation.HasMany<>(Book.class, "book_author");

}
