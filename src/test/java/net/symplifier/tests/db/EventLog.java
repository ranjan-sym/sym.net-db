package net.symplifier.tests.db;

import net.symplifier.db.Reference;
import net.symplifier.db.Row;

import java.util.Date;

/**
 * Created by ranjan on 7/3/15.
 */
public class EventLog extends Row {

  private Date      timestamp;
  private final Reference<Event> event = new Reference<>();
  private final Reference<Location> location = new Reference<>();

  public Date getTimestamp(){
    return timestamp;
  }

  public Event getEvent() {
    return event.get();
  }

  public Location getLocation() {
    return location.get();
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void setEvent(Event event) {
    this.event.set(event);
  }

  public void setLocation(Location location) {
    this.location.set(location);
  }

}
