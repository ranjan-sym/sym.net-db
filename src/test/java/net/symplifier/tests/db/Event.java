package net.symplifier.tests.db;

import net.symplifier.db.Field;
import net.symplifier.db.Row;
import net.symplifier.db.helper.AbstractRow;

/**
 * Created by ranjan on 7/3/15.
 */
public class Event extends AbstractRow {

  private final Field<String> code = new Field<>();
  private final Field<String> type = new Field<>();
  private final Field<String> description = new Field<>();
  private final Field<Boolean> triggerOn = new Field<>();

  public Event() {

  }

  public Event(String code, String type, String description, Boolean triggerOn) {
    this.code.set(code);
    this.type.set(type);
    this.description.set(description);
    this.triggerOn.set(triggerOn);
  }

  public String getCode() {
    return code.get();
  }

  public String getType() {
    return type.get();
  }

  public String getDescription() {
    return description.get();
  }

  public Boolean getTriggerOn() {
    return triggerOn.get();
  }

  public void setCode(String code) {
    this.code.set(code);
  }

  public void setType(String type) {
    this.type.set(type);
  }

  public void setDescription(String description) {
    this.description.set(description);
  }

  public void setTriggerOn(Boolean triggerOn) {
    this.triggerOn.set(triggerOn);
  }


}
