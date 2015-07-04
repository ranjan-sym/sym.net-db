package net.symplifier.tests.db.system;

import net.symplifier.db.Driver;
import net.symplifier.db.Schema;

/**
 * Created by ranjan on 7/3/15.
 */
public class TestSystem extends Schema {
  public TestSystem(Driver driver) {
    super(driver);
  }

  public final EventModel Event = new EventModel(this, "event");
  public final LocationModel Location = new LocationModel(this, "location");
  public final GeneratorModel Generator = new GeneratorModel(this, "generator", Location);

  public final EventLogModel EventLog = new EventLogModel(this, "event_log");


}
