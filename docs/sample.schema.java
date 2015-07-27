
class MySchema extends Schema {

  public final UserModel      User      = new UserModel();
  public final BookModel      Book      = new BookModel();
  public final PublisherModel Publisher = new PublisherModel();
  public final AuthorModel    Author    = new AuthorModel();

  // Do not cache observations
  public final ObservationModel Observation = new ObservationModel();

}

class User extends Row {

  private String username;

  private Password password;

}

class UserModel extends Model<User> {
  public final Column<String> username = new Column<>("username");
  public final Column<Password> password = new Column<>("password");

}

class Publisher extends Row {
  private String name;
}

class PublisherModel extends Model<Publisher> {
  public static final Column<String> name = new Column<>("name");
}

class Book extends Row {
  private String isbn;
  private String title;
  private Publisher publisher;
  private Double price;

  private Query<Author> authors = new Intermediate("")
}

class BookModel extends Model<Book> {
  public final Column<String> isbn = new Column<>("isbn");
  public final Column<String> title = new Column<>("title");
  public final Column<Publisher> publisher = new Column<>("publisher_id");
}

books = schema.Book.title.like("Programming C%");
books = schema.Book.price.lessThan(20).and(schema.Book.title.like('%PHP%'));
books = schema.Book.publisher.

