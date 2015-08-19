package net.symplifier.db.driver.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The JDBCField responsible for getting data from resultset
 *
 * Created by ranjan on 8/16/15.
 */
public interface JDBCField<T> {

  T get(ResultSet rs, int index) throws SQLException;

  class Int implements JDBCField<Integer> {
    @Override
    public Integer get(ResultSet rs, int index) throws SQLException {
      int v = rs.getInt(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Str implements JDBCField<String> {

    @Override
    public String get(ResultSet rs, int index) throws SQLException {
      String v = rs.getString(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Long implements JDBCField<java.lang.Long> {

    @Override
    public java.lang.Long get(ResultSet rs, int index) throws SQLException {
      java.lang.Long v = rs.getLong(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Float implements JDBCField<java.lang.Float> {

    @Override
    public java.lang.Float get(ResultSet rs, int index) throws SQLException {
      java.lang.Float v = rs.getFloat(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Double implements JDBCField<java.lang.Double> {

    @Override
    public java.lang.Double get(ResultSet rs, int index) throws SQLException {
      java.lang.Double v = rs.getDouble(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Date implements JDBCField<java.util.Date> {

    @Override
    public java.util.Date get(ResultSet rs, int index) throws SQLException {
      java.util.Date v = rs.getDate(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Boolean implements JDBCField<java.lang.Boolean> {

    @Override
    public java.lang.Boolean get(ResultSet rs, int index) throws SQLException {
      java.lang.Boolean v = rs.getBoolean(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return v;
      }
    }
  }

  class Blob implements JDBCField<byte[]> {

    @Override
    public byte[] get(ResultSet rs, int index) throws SQLException {
      java.sql.Blob blob = rs.getBlob(index);
      if (rs.wasNull()) {
        return null;
      } else {
        return blob.getBytes(0, (int)blob.length());
      }
    }
  }
}
