package net.symplifier.db.exceptions;

/**
 * Created by ranjan on 8/6/15.
 */
public class DatabaseException extends RuntimeException {
  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }
}
