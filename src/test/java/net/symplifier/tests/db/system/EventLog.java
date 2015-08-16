package net.symplifier.tests.db.system;

import net.symplifier.db.Schema;
import net.symplifier.db.SchemaModel;
import net.symplifier.db.columns.ReferenceColumn;
import net.symplifier.db.columns.concrete.DateColumn;

/**
 * Created by ranjan on 7/3/15.
 */
public class EventLog extends SchemaModel {

  public static final DateColumn timestamp = new DateColumn<>("timestamp", EventLog.class);
  public static final ReferenceColumn<EventLog, Event> event = new ReferenceColumn<>("event_id", EventLog.class);
  public static final ReferenceColumn<EventLog, Location> location = new ReferenceColumn<>("location_id", EventLog.class);


  public EventLog(Schema schema) {
    super(schema);
  }
}
