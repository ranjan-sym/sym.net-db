package net.symplifier.db.driver.jdbc.mysql;

import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBC;
import net.symplifier.db.exceptions.DatabaseException;

/**
 * Created by ranjan on 8/17/15.
 */
public class MySQL implements JDBC {
  public static final int DEFAULT_PORT = 3306;

  private final String database;
  private String host = "localhost";
  private int port = DEFAULT_PORT;
  private String username = null;
  private String password = null;


  public MySQL(String database) {
    this("com.mysql.jdbc.Driver", database);
  }

  public MySQL(String mysqlDriverClass, String database) {
    try {
      Class.forName(mysqlDriverClass);
    } catch(ClassNotFoundException e) {
      throw new DatabaseException("Could not find mysql jdbc driver in application", e);
    }
    this.database = database;
  }

  public MySQL host(String host) {
    this.host = host;
    return this;
  }

  public MySQL port(int port) {
    this.port = port;
    return this;
  }

  public MySQL username(String username) {
    this.username = username;
    return this;
  }

  public MySQL password(String password) {
    this.password = password;
    return this;
  }

  String getUri() {
    StringBuilder b = new StringBuilder("jdbc:mysql://");
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
  public MySQLDriver build(Schema schema) {
    return new MySQLDriver(schema, getUri(), username, password);
  }


}
