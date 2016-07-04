package net.symplifier.db.driver.jdbc.postgres;

import net.symplifier.db.Column;
import net.symplifier.db.ModelStructure;
import net.symplifier.db.Query;
import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBCDriver;
import net.symplifier.db.driver.jdbc.JDBCParameter;
import net.symplifier.db.driver.jdbc.SQLBuilder;
import net.symplifier.db.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ranjan on 8/17/15.
 */
public class PostgreSQLDriver extends JDBCDriver {
  protected PostgreSQLDriver(Schema schema, String uri, String username, String password) {
    super(schema, uri, username, password);
  }

  @Override
  protected String quote(String name) {
    return "\"" + name + "\"";
  }

  @Override
  protected String getTypeName(Class<?> type) {
    if (type == Integer.class) {
      return "INT";
    } else if (type == Float.class) {
      return "REAL";
    } else if (type == Double.class) {
      return "DOUBLE PRECISION";
    } else if (type == Long.class) {
      return "BIGINT";
    } else if (type == Boolean.class) {
      return "BOOLEAN";
    } else if (type == String.class) {
      return "TEXT";
    } else if (type == Date.class) {
      return "TIMESTAMP WITH TIME ZONE";
    } else if (type == byte[].class) {
      return "BYTEA";
    } else {
      throw new DatabaseException("Unknown column type " + type.getTypeName(), null);
    }
  }

  @Override
  public void createModel(ModelStructure structure) {
//    try (Connection conn = dataSource.getConnection()) {
//      ResultSet rs = conn.createStatement().executeQuery("SELECT to_regclass('public." + structure.getTableName() + "')");
//      rs.next();
//      String regClass = rs.getString(1);
//      conn.close();
//
//      if (regClass != null && !regClass.equals("NULL")) {
//        LOGGER.info("Table " + structure.getTableName() + " exists");
//        return;
//      }
//    } catch(SQLException e) {
//      throw new DatabaseException("Error while executing CREATE DDL", e);
//    }

    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE IF NOT EXISTS ");

    builder.append(quote(structure.getTableName()));
    builder.append("(");
    for(int i=0; i<structure.getColumnCount(); ++i) {
      if (i > 0) {
        builder.append(',');
      }

      Column col = structure.getColumn(i);
      builder.append("\r\n\t");
      builder.append(quote(col.getFieldName()));
      builder.append(" ");
      if (col instanceof Column.Primary) {
        builder.append("BIGSERIAL");
      } else {
        builder.append(getTypeName(col.getValueType()));
      }

      if (col.isPrimary()) {
        builder.append(" PRIMARY KEY");
      }


      if (!col.canBeNull()) {
        builder.append(" NOT NULL");
        Object def = col.getDefaultValue();
        if (def != null) {
          builder.append(" DEFAULT ");
          if (def instanceof Number) {
            builder.append(def.toString());
          } else if (def instanceof Date) {
            builder.append('"');
            builder.append(Schema.ISO_8601_DATE_TIME.format(def));
            builder.append('"');
          } else if (def instanceof Boolean) {
            builder.append( def );
          } else {
            builder.append("'");
            builder.append(col.getDefaultValue());
            builder.append("'");
          }
        }
      }
    }
    builder.append("\r\n);");

    String sql = builder.toString();
    System.out.println(sql);

    try (Connection conn = dataSource.getConnection()) {
      conn.createStatement().execute(sql);
      conn.close();
    } catch(SQLException e) {
      throw new DatabaseException("Error while executing CREATE DDL", e);
    }

    // Create all the indexes as well
    List<Column.Index> indexes = structure.getIndexes();
    for(Column.Index index:indexes) {
      String indexName = "IDX_" + structure.getTableName() + "_" + index.getName();
      try (Connection conn = dataSource.getConnection()) {
        ResultSet rs = conn
            .createStatement()
            .executeQuery("SELECT COUNT(*) FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relname = '" + indexName.toLowerCase() + "' AND n.nspname = 'public'");
        rs.next();
        int count = rs.getInt(1);
        conn.close();

        if (count != 0) {
          LOGGER.info("Index " + indexName + " exists");
          return;
        }
      } catch(SQLException e) {
        throw new DatabaseException("Error while executing CREATE DDL", e);
      }
      builder = new StringBuilder();
      builder.append("CREATE ");
      if (index.isUnique()) {
        builder.append("UNIQUE ");
      }
      builder.append("INDEX IDX_");
      builder.append(structure.getTableName());
      builder.append("_");
      builder.append(index.getName());
      builder.append(" ON ");
      builder.append(structure.getTableName());
      builder.append("(");
      for(int i=0; i<index.getColumnCount(); ++i) {
        if (i > 0) {
          builder.append(',');
        }
        builder.append(index.getColumn(i).getFieldName());
      }
      builder.append(");");

      try (Connection conn = dataSource.getConnection()) {
        sql = builder.toString();
        System.out.println(sql);
        conn.createStatement().execute(sql);
        conn.close();
      } catch(SQLException e) {
        throw new DatabaseException("Error while executing CREATE INDEX", e);
      }
    }



  }

  @Override
  public Long doInsert(ModelStructure modelStructure, Object[] data) {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      throw new DatabaseException("No connection to database", null);
    }

    boolean needId = data[0] == null;
    List<Column> columns = new ArrayList<>(modelStructure.getColumns());

    if (columns.size() == 1 && needId) {
      // This will need a raw query
      String query = "INSERT INTO \"" + modelStructure.getTableName() + "\" VALUES(DEFAULT)";

      try {
        Statement statement = connection.createStatement();
        int rows = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        if (rows == 0) {
          throw new DatabaseException("Inserting record failed", null);
        }

        try (ResultSet resultSet = statement.getGeneratedKeys()) {
          if (resultSet.next()) {
            return resultSet.getLong(1);
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        try {
          connection.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

    SQLBuilder sql = new SQLBuilder(this);

    sql.append("INSERT INTO ").append(modelStructure);
    if (needId) {
      List<Object> dataList = new ArrayList<>(Arrays.asList(data));
      int idx = -1;
      for (int i = 0; i < columns.size(); i++) {
        if (columns.get(i).isPrimary()) {
          idx = i;
          break;
        }
      }

      if (idx != -1) {
        columns.remove(idx);
        dataList.remove(idx);
      }

      sql.append(columns, dataList.toArray(), 0);
    } else {
      sql.append(columns, data, 0);
    }


    String sqlText = sql.getSQL();
    System.out.println(sqlText);
    try (PreparedStatement statement = connection.prepareStatement(sqlText,
            needId ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS )) {
      List<Query.Parameter> parameters = sql.getParameters();
      for (int i = 0; i < parameters.size(); ++i) {
        Query.Parameter parameter = parameters.get(i);
        JDBCParameter p = (JDBCParameter) parameter.getSetter();
        p.set(statement, i + 1, parameter.getDefault());
      }

      int affectedRows = statement.executeUpdate();
      if (affectedRows == 0) {
        throw new DatabaseException("Inserting record failed", null);
      }
      if (needId) {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
          }
        }
      } else {
        return (Long) data[0];
      }
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while trying to insert record", e);
    } finally {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
