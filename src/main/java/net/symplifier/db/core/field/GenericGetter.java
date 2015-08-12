package net.symplifier.db.core.field;

/**
 * Created by ranjan on 7/31/15.
 */
public interface GenericGetter extends Getter<Integer> {
  Integer getInteger(Integer index);

  String getString(Integer index);

  byte[] getBlob(Integer index);
}
