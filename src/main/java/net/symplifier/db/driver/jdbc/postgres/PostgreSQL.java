package net.symplifier.db.driver.jdbc.postgres;

import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBC;
import net.symplifier.db.exceptions.DatabaseException;

/**
 * Created by ranjan on 8/17/15.
 */
public class PostgreSQL implements JDBC {
  public static final int DEFAULT_PORT = 5432;
  private final String database;
  private String host = "localhost";
  private int port = DEFAULT_PORT;
  private String username;
  private String password;

  public PostgreSQL(String database) {
    this("org.postgresql.Driver", database);
  }

  public PostgreSQL(String driverClassName, String database) {
    this.database = database;
    try {
      Class.forName(driverClassName);
    } catch(ClassNotFoundException e) {
      throw new DatabaseException("PostgreSQL driver not found", e);
    }
  }

  public PostgreSQL host(String host) {
    this.host = host;
    return this;
  }

  public PostgreSQL port(int port) {
    this.port = port;
    return this;
  }

  public PostgreSQL username(String username) {
    this.username = username;
    return this;
  }

  public PostgreSQL password(String password) {
    this.password = password;
    return this;
  }

  private String getUri() {
    StringBuilder b = new StringBuilder();
    b.append("jdbc:postgresql://");
    b.append(host);
    if (port != DEFAULT_PORT) {
      b.append(':');
      b.append(port);
    }
    b.append('/');
    b.append(database);
    return b.toString();
  }

  @Override
  public PostgreSQLDriver build(Schema schema) {
    return new PostgreSQLDriver(schema, getUri(), username, password);
  }
}
