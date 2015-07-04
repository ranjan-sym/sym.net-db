package net.symplifier.tests.db.system;

import net.symplifier.db.Column;
import net.symplifier.db.Driver;
import net.symplifier.db.Model;
import net.symplifier.db.Schema;
import net.symplifier.tests.db.Location;

/**
 * Created by ranjan on 7/3/15.
 */
public class LocationModel extends Model<Location> {

  public static final Column<String> name = new Column<>("name", String.class);

  public LocationModel(Schema schema, String name) {
    super(schema, name);
  }
}
