package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public class DatabaseException extends Exception {
  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
