package net.symplifier.db.driver.jdbc.sqlite;

import net.symplifier.db.driver.jdbc.JDBCDriver;

/**
 * Created by ranjan on 8/17/15.
 */
public class SqliteDriver extends JDBCDriver {


  protected SqliteDriver(String uri, String username, String password) {
    super(uri, username, password);
  }
}
