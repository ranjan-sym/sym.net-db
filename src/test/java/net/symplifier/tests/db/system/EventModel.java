package net.symplifier.tests.db.system;

import net.symplifier.db.*;
import net.symplifier.tests.db.Event;

/**
 * Created by ranjan on 7/3/15.
 */
public class EventModel extends Model<Event> {

  public static final Column<String> code = new Column<>("code", String.class);
  public static final Column<String> type = new Column<>("type", String.class);
  public static final Column<String> description = new Column<>("description", String.class);
  public static final Column<Boolean> triggerOn = new Column<>("trigger_on", Boolean.class);

  public EventModel(Schema schema, String name) {
    super(schema, name);
  }
}
