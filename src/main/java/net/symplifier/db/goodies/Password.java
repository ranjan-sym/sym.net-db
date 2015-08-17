package net.symplifier.db.goodies;

import net.symplifier.db.Model;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * A Password type field for use in ORM that uses SHA-256 for hashing and
 * BLOB for storage
 *
 * Created by ranjan on 7/28/15.
 */
public class Password extends net.symplifier.db.Column.CustomType<byte[]> {

  public static class Column<M extends Model> extends net.symplifier.db.Column.Custom<M, byte[], Password> {

    public Column() {
      super(Password.class, byte[].class);
    }

    @Override
    public Password getFromGeneric(byte[] value) {
      return new Password(value);
    }

    @Override
    public byte[] getGeneric(Password value) {
      return value.getGeneric();
    }

  }

  public Password(byte[] src) {
    super(Arrays.copyOf(src, src.length));
  }

  public static Password create(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return new Password(md.digest(password.getBytes(Charset.defaultCharset())));
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }

  @Override
  public boolean equals(Object pwd) {
    if (pwd instanceof String) {
      return equals(Password.create((String) pwd));
    } else
      return pwd instanceof Password
              && Arrays.equals(super.genericValue, ((Password) pwd).genericValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (byte b : genericValue) {
      sb.append(String.format("%02x", (b & 0xff)));
    }
    return sb.toString();
  }
}
