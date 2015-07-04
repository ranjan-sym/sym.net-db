package net.symplifier.tests.db;

import net.symplifier.db.Row;

/**
 * Created by ranjan on 7/3/15.
 */
public class Location extends Row {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
