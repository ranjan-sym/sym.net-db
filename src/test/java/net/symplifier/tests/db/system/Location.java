package net.symplifier.tests.db.system;

import net.symplifier.db.Schema;
import net.symplifier.db.SchemaModel;
import net.symplifier.db.columns.concrete.StringColumn;

/**
 * Created by ranjan on 7/3/15.
 */
public class Location extends SchemaModel {

  public static final StringColumn<Location> name = new StringColumn<>("name", Location.class);

  public Location(Schema schema) {
    super(schema);
  }
}
