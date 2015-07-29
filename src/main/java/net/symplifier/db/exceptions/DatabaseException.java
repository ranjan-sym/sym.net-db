package net.symplifier.db.exceptions;

/**
 * Created by ranjan on 7/28/15.
 */
public class DatabaseException extends RuntimeException {
  public DatabaseException(String message) {
    super(message);
  }

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
