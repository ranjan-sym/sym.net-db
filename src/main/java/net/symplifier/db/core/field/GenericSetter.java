package net.symplifier.db.core.field;

/**
 * Created by ranjan on 7/31/15.
 */
public interface GenericSetter extends Setter<Integer> {

  void setInteger(Integer position, Integer value);

  void setLong(Integer position, Long value);

  void setString(Integer position, String value);

  void setBlob(Integer position, byte[] value);
}
