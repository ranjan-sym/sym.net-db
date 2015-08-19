package net.symplifier.db.driver.jdbc.postgres;

import net.symplifier.db.driver.jdbc.JDBCDriver;

/**
 * Created by ranjan on 8/17/15.
 */
public class PostgreSQLDriver extends JDBCDriver {
  protected PostgreSQLDriver(String uri, String username, String password) {
    super(uri, username, password);
  }
}
