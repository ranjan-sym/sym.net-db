package net.symplifier.db.exceptions;

import net.symplifier.db.Model;

/**
 * Created by ranjan on 7/28/15.
 */
public class ModelException extends RuntimeException {
  private final Class<? extends Model> modelClass;

  public ModelException(Class<? extends Model> modelClass, String message) {
    super(message);
    this.modelClass = modelClass;
  }

  public ModelException(Class<? extends Model> modelClass, String message, Throwable cause) {
    super(message, cause);
    this.modelClass = modelClass;
  }

}
