package net.symplifier.db;

/**
 * Created by ranjan on 7/3/15.
 */
public class ModelException extends RuntimeException {
  public ModelException(String message) {
    super(message);
  }

  public ModelException(String message, Throwable cause) {
    super(message, cause);
  }
}
