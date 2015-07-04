package net.symplifier.tests.db.system;

import net.symplifier.db.*;
import net.symplifier.tests.db.Event;

/**
 * Created by ranjan on 7/3/15.
 */
public class EventModel extends Model<Event> {

  public static final Column code = new Column("code", String.class);
  public static final Column type = new Column("type", String.class);
  public static final Column description = new Column("description", String.class);
  public static final Column triggerOn = new Column("trigger_on", Boolean.class);

  public EventModel(Schema schema, String name) {
    super(schema, name);
  }
}
