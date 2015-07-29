package net.symplifier.db.goodies;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by ranjan on 7/28/15.
 */
public class Password {
  public static final Charset UTF8 = Charset.forName("utf8");

  private final byte[] shaPassword;

  public Password(byte[] src) {
    this.shaPassword = Arrays.copyOf(src, src.length);
  }

  public Password(String password) {
    byte[] sha = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      sha = md.digest(password.getBytes(UTF8));
    } catch (NoSuchAlgorithmException e) {

    }

    shaPassword = sha;
  }

  @Override
  public boolean equals(Object pwd) {
    if (pwd instanceof String) {
      return equals(new Password((String)pwd));
    } else if(pwd instanceof Password) {
      return Arrays.equals(shaPassword, ((Password)pwd).shaPassword);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (byte b : shaPassword) {
      sb.append(String.format("%02x", (b & 0xff)));
    }
    return sb.toString();
  }
}
