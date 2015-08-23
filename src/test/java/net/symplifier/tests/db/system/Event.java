package net.symplifier.tests.db.system;

import net.symplifier.db.*;

import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public class Event extends ModelInstance<Event> {

  public static final Query.Builder<Event> Query = new Query.Builder<>(Event.class);

  public static final Column.Primary<Event> id = new Column.Primary<>();

  public static final Column.Text<Event> code = new Column.Text<>();
  public static final Column.Text<Event> type = new Column.Text<>();
  public static final Column.Text<Event> description = new Column.Text<>();
  public static final Column.Bool<Event> triggerOn = new Column.Bool<>();


  public static final Relation.HasMany<Event, EventLog> logs =
          new Relation.HasMany<>(EventLog.class, EventLog.event);


  public String getCode() {
    return get(code);
  }

  public void setCode(String value) {
    set(code, value);
  }

  public String getType() {
    return get(type);
  }

  public void setType(String value) {
    set(type, value);
  }

  public String getDescription() {
    return get(description);
  }

  public void setDescription(String value) {
    set(description, value);
  }

  public Boolean getTriggerOn() {
    return get(triggerOn);
  }

  public void setTriggerOn(Boolean value) {
    set(triggerOn, value);
  }

  public List<EventLog> getLogs() {
    return get(logs);
  }

}
