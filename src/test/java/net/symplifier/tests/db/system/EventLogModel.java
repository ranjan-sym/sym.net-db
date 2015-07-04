package net.symplifier.tests.db.system;

import net.symplifier.db.Column;
import net.symplifier.db.Model;
import net.symplifier.db.Driver;
import net.symplifier.db.Schema;
import net.symplifier.tests.db.Event;
import net.symplifier.tests.db.EventLog;
import net.symplifier.tests.db.Location;

import java.util.Date;

/**
 * Created by ranjan on 7/3/15.
 */
public class EventLogModel extends Model<EventLog> {

  public static final Column<Date> timestamp = new Column<>("timestamp", Date.class);

  public static final Column<Event> event = new Column<>("event_id", Event.class);

  public static final Column<Location> location = new Column<>("location_id", Location.class);


  public EventLogModel(Schema schema, String name) {
    super(schema, name);

  }
}
