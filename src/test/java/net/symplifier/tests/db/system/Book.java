package net.symplifier.tests.db.system;

import net.symplifier.db.*;

import java.security.PublicKey;

/**
 * Created by ranjan on 8/20/15.
 */
public class Book extends ModelInstance<Book> {

  public static Query.Builder<Book> Q() { return new Query.Builder<>(Book.class); }

  public static final Column.Primary<Book> id = new Column.Primary<>();
  public static final Column.Text<Book> title = new Column.Text<>();
  public static final Column.Text<Book> isbn = new Column.Text<>();
  public static final Column.Reference<Book, Publisher> publisher = new Column.Reference<>(Publisher.class);

  public static final Relation.HasMany<Book, Author> authors = new Relation.HasMany<>(Author.class, "book_author");

  public String getTitle() {
    return get(title);
  }

  public String getISBN() {
    return get(isbn);
  }

  public Publisher getPublisher() {
    return getReference(publisher);
  }

  public void setTitle(String value) {
    set(title, value);
  }

  public void setISBN(String value) {
    set(isbn, value);
  }

  public void setPublisher(Publisher value) {
    setReference(publisher, value);
  }

  public void addAuthor(Author author) {
    super.add(authors, author);
  }

}
