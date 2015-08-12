package net.symplifier.db;

import net.symplifier.db.columns.Column;
import net.symplifier.db.columns.ReferenceColumn;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ranjan on 8/6/15.
 */
public abstract class SchemaModel implements Model {
  /* The main schema to which this model belongs */
  private final Schema schema;

  /* All the fields for the columns defined for this model */
  private final Map<Column, Field> fields = new HashMap<Column, Field>();


  public SchemaModel(Schema schema) {
    this.schema = schema;
  }

  @Override
  public Schema getSchema() {
    return schema;
  }

  @Override
  public <T> Field<T, ?, ?, ?> getField(Column<?, T> column) {
    return null;
  }

  @Override
  public <T extends Model> Reference<T> getReference(ReferenceColumn<?, T> column) {
    return null;
  }

  public void save() {

  }
}
