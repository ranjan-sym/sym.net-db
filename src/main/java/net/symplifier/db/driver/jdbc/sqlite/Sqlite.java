package net.symplifier.db.driver.jdbc.sqlite;

import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBC;
import net.symplifier.db.exceptions.DatabaseException;

/**
 * Created by ranjan on 8/17/15.
 */
public class Sqlite implements JDBC {
  private final String file;

  public Sqlite(String filename) {
    this("org.sqlite.JDBC",filename);
  }

  public Sqlite(String sqliteDriverClass, String filename) {
    this.file = filename;

    try {
      Class.forName(sqliteDriverClass);
    } catch(ClassNotFoundException e) {
      throw new DatabaseException("Sqlite driver not found", e);
    }
  }

  @Override
  public SqliteDriver build(Schema schema) {
    return new SqliteDriver(schema, "jdbc:sqlite:" + file, null, null);
  }
}
