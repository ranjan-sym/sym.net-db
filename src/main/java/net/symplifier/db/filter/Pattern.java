package net.symplifier.db.filter;

/**
 * Created by ranjan on 7/30/15.
 */
public class Pattern implements Entity {
  private final String pattern;
  public Pattern(String pattern) {
    this.pattern = pattern;
  }

  public String getPattern() {
    return pattern;
  }
}
