package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public class Row {
  private Schema schema;
  private Long id;

  void setSchema(Schema schema) {
    this.schema = schema;
  }

  public final Long getId() {
    return id;
  }

  void setId(long id) {
    this.id = id;
  }

  public void save() throws DatabaseException {
    if (schema == null) {
      throw new ModelException("The '" + getClass() + "' instance doesn't " +
              "seem to be generated with a Schema. Make sure the instance is " +
              "generated through the query in the Schema or create a new " +
              "instance using Schema#createRow method");
    } else {
      schema.save(this);
    }
  }
}
