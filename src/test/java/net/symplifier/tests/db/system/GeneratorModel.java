package net.symplifier.tests.db.system;

import net.symplifier.db.Column;
import net.symplifier.db.Model;
import net.symplifier.db.Driver;
import net.symplifier.db.Schema;
import net.symplifier.tests.db.Generator;

/**
 * Created by ranjan on 7/3/15.
 */
public class GeneratorModel extends Model<Generator> {

  public static final Column<Double> capacity = new Column<>("capacity", Double.class);

  public GeneratorModel(Schema schema, String name, LocationModel parent) {
    super(schema, name, parent);
  }
}
