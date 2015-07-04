package net.symplifier.tests.db;

/**
 * Created by ranjan on 7/3/15.
 */
public class Generator extends Location {

  private Double capacity;

  public Generator() {

  }

  public Generator(String name, Double capacity) {
    super.setName(name);
    this.capacity = capacity;
  }

  public Double getCapacity() {
    return capacity;
  }

  public void setCapacity(Double capacity) {
    this.capacity = capacity;
  }

  public String toString() {
    return getId() + " - " + getName() + " - " + capacity;
  }
}
