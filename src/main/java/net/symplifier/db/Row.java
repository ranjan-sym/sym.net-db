package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public class Row {
  private Schema schema;
  private Long id;

  protected Schema getSchema() {
    return schema;
  }

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
      onSaved();
    }
  }

  /**
   * Callback mechanism for the implementing class to perform certain action
   * when the row is loaded from the database system
   */
  public void onLoaded() {

  }

  /**
   * Callback mechanism for the implementing class to perform certain action
   * when the row is aved to the database system
   */
  public void onSaved() {

  }
}
