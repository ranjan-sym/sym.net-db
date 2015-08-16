package net.symplifier.tests.db.system;

import net.symplifier.db.Schema;
import net.symplifier.db.columns.concrete.DoubleColumn;

/**
 * Created by ranjan on 7/3/15.
 */
public class Generator extends Location {

  public static final DoubleColumn<Generator> capacity = new DoubleColumn<>("capacity", Generator.class);

  public Generator(Schema schema) {
    super(schema);
  }
}
