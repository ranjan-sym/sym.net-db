package net.symplifier.db.driver.jdbc.mysql;

import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBCDriver;

/**
 * Created by ranjan on 8/17/15.
 */
public class MySQLDriver extends JDBCDriver {

  MySQLDriver(Schema schema, String uri, String username, String password) {
    super(schema, uri, username, password);
  }
}
