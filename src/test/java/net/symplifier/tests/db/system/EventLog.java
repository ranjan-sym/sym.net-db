package net.symplifier.tests.db.system;

import net.symplifier.db.Column;
import net.symplifier.db.ModelInstance;
import net.symplifier.db.ModelStructure;
import net.symplifier.db.Schema;

/**
 * Created by ranjan on 7/3/15.
 */
public class EventLog extends ModelInstance {

  public static final Column.Primary<EventLog> id = new Column.Primary<>();

  public static final Column.Reference<EventLog, Location> location = new Column.Reference<>(Location.class);
  public static final Column.Reference<EventLog, Event> event = new Column.Reference<>(Event.class);
  public static final Column.Text<EventLog> name = new Column.Text<>();

//  public static final DateColumn timestamp = new DateColumn<>("timestamp", EventLog.class);
//  public static final ReferenceColumn<EventLog, Event> event = new ReferenceColumn<>("event_id", EventLog.class);
//  public static final ReferenceColumn<EventLog, Location> location = new ReferenceColumn<>("location_id", EventLog.class);
//
//
//  public EventLog(Schema schema) {
//    super(schema);
//  }
}
