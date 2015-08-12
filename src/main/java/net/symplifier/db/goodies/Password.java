package net.symplifier.db.goodies;

import net.symplifier.db.Model;
import net.symplifier.db.columns.CustomColumn;
import net.symplifier.db.query.Query;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by ranjan on 7/28/15.
 */
public class Password extends CustomColumn.CustomFieldType<byte[]> {
  public static class Column<M extends Model> extends CustomColumn<M, byte[], Password> {

    public Column(String name, Class<M> modelType) {
      super(name, modelType);
    }

    @Override
    public Class<Password> getType() {
      return Password.class;
    }

    @Override
    public Class<byte[]> getGenericType() {
      return byte[].class;
    }

    @Override
    public void buildQuery(Query query, StringBuilder res) {

    }
  }

  public static final Charset UTF8 = Charset.forName("utf8");

  public Password(byte[] src) {
    super(Arrays.copyOf(src, src.length));
  }

  public static Password create(String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return new Password(md.digest(password.getBytes()));
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }

  @Override
  public boolean equals(Object pwd) {
    if (pwd instanceof String) {
      return equals(Password.create((String)pwd));
    } else if(pwd instanceof Password) {
      return Arrays.equals(super.genericValue, ((Password)pwd).genericValue);
    } else {
      return false;
    }
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
