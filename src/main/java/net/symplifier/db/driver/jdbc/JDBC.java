package net.symplifier.db.driver.jdbc;

import net.symplifier.db.Schema;

/**
 * Created by ranjan on 8/17/15.
 */
public interface JDBC {
  JDBCDriver build(Schema schema);
}
