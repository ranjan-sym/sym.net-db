package net.symplifier.tests.db;

import net.symplifier.db.Row;

/**
 * Created by ranjan on 7/3/15.
 */
public class Event extends Row {
  private String code;
  private String type;
  private String description;
  private Boolean triggerOn;

  public Event() {

  }

  public Event(String code, String type, String description, Boolean triggerOn) {
    this.code = code;
    this.type = type;
    this.description = description;
    this.triggerOn = triggerOn;
  }

  public String getCode() {
    return code;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }

  public Boolean getTriggerOn() {
    return triggerOn;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setTriggerOn(Boolean triggerOn) {
    this.triggerOn = triggerOn;
  }


}
