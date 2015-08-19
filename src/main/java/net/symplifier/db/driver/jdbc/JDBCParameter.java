package net.symplifier.db.driver.jdbc;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * The JDBCParameter used for setting parameter values in PreparedStatement
 *
 * Created by ranjan on 8/16/15.
 */
public interface JDBCParameter<V> {

  void set(PreparedStatement statement, int index, V value) throws SQLException ;



  class Int implements JDBCParameter<Integer> {

    @Override
    public void set(PreparedStatement statement, int index, Integer value) throws SQLException {
      if (value == null) {
        statement.setNull(index, Types.INTEGER);
      } else {
        statement.setInt(index, value);
      }
    }
  }

  class Float implements JDBCParameter<java.lang.Float> {

    @Override
    public void set(PreparedStatement statement, int index, java.lang.Float value) throws SQLException {
      if(value == null) {
        statement.setNull(index, Types.FLOAT);
      } else {
        statement.setFloat(index, value);
      }
    }
  }

  class Double implements JDBCParameter<java.lang.Double> {

    @Override
    public void set(PreparedStatement statement, int index, java.lang.Double value) throws SQLException {
      if (value == null) {
        statement.setNull(index, Types.DOUBLE);
      } else {
        statement.setDouble(index, value);
      }
    }
  }

  class Str implements JDBCParameter<String> {

    @Override
    public void set(PreparedStatement statement, int index, String value) throws SQLException {
      if (value == null) {
        statement.setNull(index, Types.VARCHAR);
      } else {
        statement.setString(index, value);
      }
    }
  }

  class Boolean implements JDBCParameter<java.lang.Boolean> {

    @Override
    public void set(PreparedStatement statement, int index, java.lang.Boolean value) throws SQLException {
      if (value == null) {
        statement.setNull(index, Types.BOOLEAN);
      } else {
        statement.setBoolean(index, value);
      }
    }
  }

  class Date implements JDBCParameter<java.util.Date> {

    @Override
    public void set(PreparedStatement statement, int index, java.util.Date value) throws SQLException {
      if (value == null) {
        statement.setNull(index, Types.DATE);
      } else {
        statement.setDate(index, new java.sql.Date(value.getTime()));
      }
    }
  }

  class Blob implements JDBCParameter<byte[]> {

    @Override
    public void set(PreparedStatement statement, int index, byte[] value) throws SQLException {
      if (value == null) {
        statement.setNull(index, Types.BLOB);
      } else {
        statement.setBlob(index, new SerialBlob(value));
      }
    }
  }

}
