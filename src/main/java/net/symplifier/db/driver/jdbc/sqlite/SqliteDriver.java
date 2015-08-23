package net.symplifier.db.driver.jdbc.sqlite;

import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBCDriver;

/**
 * Created by ranjan on 8/17/15.
 */
public class SqliteDriver extends JDBCDriver {


  protected SqliteDriver(Schema schema, String uri, String username, String password) {
    super(schema, uri, username, password);
  }

  @Override
  protected String getTypeName(Class type) {
    if (type == Long.class) {
      return "INTEGER";
    } else {
      return super.getTypeName(type);
    }
  }
}
