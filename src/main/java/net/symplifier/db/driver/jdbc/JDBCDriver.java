package net.symplifier.db.driver.jdbc;

import net.symplifier.core.application.Session;
import net.symplifier.db.*;
import net.symplifier.db.exceptions.DatabaseException;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The JDBCDriver for ORM
 *
 * Created by ranjan on 7/28/15.
 */
public abstract class JDBCDriver implements Driver, Session.Listener {
  private DataSource dataSource;

  @Override
  public JDBCParameter getParameterSetter(Class type) {
    return PARAMETER_SETTERS.get(type);
  }

  public JDBCField getField(Class type) {
    return FIELDS.get(type);
  }

  private static final Map<Class, JDBCParameter> PARAMETER_SETTERS = new HashMap<Class, JDBCParameter>() {{
    put(Integer.class, new JDBCParameter.Int());
    put(String.class, new JDBCParameter.Str());
    put(Float.class, new JDBCParameter.Float());
    put(Double.class, new JDBCParameter.Double());
    put(Date.class, new JDBCParameter.Date());
    put(Boolean.class, new JDBCParameter.Boolean());
    put(byte[].class, new JDBCParameter.Blob());
  }};

  private static final Map<Class, JDBCField> FIELDS = new HashMap<Class, JDBCField>() {{
    put(Integer.class, new JDBCField.Int());
    put(String.class, new JDBCField.Str());
    put(Float.class, new JDBCField.Float());
    put(Double.class, new JDBCField.Double());
    put(Date.class, new JDBCField.Date());
    put(Boolean.class, new JDBCField.Boolean());
    put(byte[].class, new JDBCField.Blob());
  }};


  protected JDBCDriver(String uri, String username, String password) {

    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri, username, password);

    PoolableConnectionFactory poolableConnectionFactory =
            new PoolableConnectionFactory(connectionFactory, null);

    ObjectPool<PoolableConnection> connectionPool
            = new GenericObjectPool<>(poolableConnectionFactory);

    poolableConnectionFactory.setPool(connectionPool);

    dataSource = new PoolingDataSource<>(connectionPool);

    // Register the driver as a session listener for the Application
    Session.addListener(this);
  }

  private String getTypeName(Class<?> type) {
    if (type == Integer.class) {
      return "INT";
    } else if (type == Float.class) {
      return "FLOAT";
    } else if (type == Double.class) {
      return "DOUBLE";
    } else if (type == Long.class) {
      return "BIGINT";
    } else if (type == Boolean.class) {
      return "BIT";
    } else if (type == String.class) {
      return "TEXT";
    } else if (type == Date.class) {
      return "DATETIME";
    } else if (type == byte[].class) {
      return "BLOB";
    } else {
      throw new DatabaseException("Unknown column type " + type.getTypeName(), null);
    }

  }

  @Override
  public void createModel(ModelStructure structure) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE IF NOT EXISTS ");
    builder.append(structure.getTableName());
    builder.append('(');
    for(int i=0; i<structure.getColumnCount(); ++i) {
      if (i > 0) {
        builder.append(',');
      }

      Column col = structure.getColumn(i);
      builder.append("\r\n\t");
      builder.append(col.getFieldName());
      builder.append(' ');
      builder.append(getTypeName(col.getValueType()));

      if (col.isPrimary()) {
        builder.append(" PRIMARY KEY");
      }

      if (!col.canBeNull()) {
        builder.append(" NOT NULL");
      }
    }
    builder.append("\r\n)");

    String sql = builder.toString();
    System.out.println(sql);

    try {
      Connection conn = dataSource.getConnection();
      conn.createStatement().execute(sql);
    } catch(SQLException e) {
      throw new DatabaseException("Error while executing CREATE DDL", e);
    }

  }

  @Override
  public <T extends Model> JDBCQuery<T> createQuery(Query.Builder<T> builder) {
    return new JDBCQuery<>(builder);
  }

  @Override
  public void onSessionBegin(Session session) {
    try {
      session.attach(JDBCSession.class, new JDBCSession(dataSource.getConnection()));
    } catch(SQLException e) {
      throw new DatabaseException("Error while starting a new JDBC Session", e);
    }

  }

  @Override
  public void onSessionEnd(Session session) {
    JDBCSession s = session.getAttachment(JDBCSession.class);
    s.close();
  }

}