package net.symplifier.tests.db.system;

import net.symplifier.db.*;
import net.symplifier.db.columns.concrete.BooleanColumn;
import net.symplifier.db.columns.concrete.StringColumn;

/**
 * Created by ranjan on 7/3/15.
 */
public class Event extends ModelInstance {

  public static final Column<Event, String> code = new Column<>(Event.class, String.class, 1000);
  public static final Column<Event, String> type = new Column<>(Event.class, String.class);
  public static final Column<Event, String> description = new Column<>(Event.class, String.class);
  public static final Column<Event, Boolean> triggerOn = new Column<>(Event.class, Boolean.class);

  public Event(ModelStructure structure) {
    super(structure);
  }

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

}
