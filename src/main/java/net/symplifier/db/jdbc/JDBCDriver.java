package net.symplifier.db.jdbc;

import net.symplifier.db.*;
import net.symplifier.db.Driver;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.Map;

/**
 * Created by ranjan on 7/3/15.
 */
public class JDBCDriver implements Driver {
  Connection connection;

  public JDBCDriver(String connString) throws DatabaseException {
    try {
      connection = DriverManager.getConnection(connString);
    } catch(SQLException e) {
      throw new DatabaseException("Error while initializing connection ", e);
    }
  }

  public void begin() throws DatabaseException {
//    try {
//      connection.setAutoCommit(false);
//    } catch(SQLException e) {
//      throw new DatabaseException("Error while trying to start transaction", e);
//    }
  }

  public void commit() throws DatabaseException {
//    try {
//      connection.commit();
//    } catch(SQLException e) {
//      throw new DatabaseException("Error while trying to commit transaction", e);
//    }
  }

  public void rollback() throws DatabaseException {
//    try {
//      connection.rollback();
//    } catch(SQLException e) {
//      throw new DatabaseException("Error while trying to rollback transaction", e);
//    }
  }

  @Override
  public <T extends Row> Query<T> createQuery(Model<T> model) {
    return new JDBCQuery<>(this, model);
  }

  @Override
  public <T extends Row> ModelInfo<T> generateModelInfo(Model<T> model) {
    return new JDBCModelInfo<>(this, model);
  }

  @Override
  public void createModel(ModelInfo modelInfo) throws DatabaseException {
    Model<?> model = modelInfo.getModel();
    String sql = "CREATE TABLE IF NOT EXISTS " + model.getName() + "(";

    boolean first = true;

    for(Column column:model.getColumns()) {
      if (first) {
        first = false;
      } else {
        sql += ", ";
      }

      if (model.isPrimaryKey(column)) {
        sql += model.getPrimaryKeyFieldName() + " INTEGER PRIMARY KEY " + getAutoIncrementKeyword() + " NOT NULL";
        if (model.hasParent()) {
          sql += " REFERENCES " + model.getParent().getName() + "(" + model.getParent().getPrimaryKeyFieldName() + ")";
        }
      } else {
        sql += column.getName() + " " + getTypeName(column);
        if (column.isRequired()) {
          sql += " NOT NULL";
        } else {
          sql += " NULL";
        }
      }
    }

    sql += ");";

    try {
      connection.createStatement().executeUpdate(sql);
    } catch(SQLException e) {
      throw new DatabaseException("Error while creating model \"" + model.getName() + "\" using sql " + sql, e);
    }
  }

  public void save(Schema schema, Row row) throws DatabaseException {
    // First find out if there is a model associated for the given Row type
    JDBCModelInfo model = (JDBCModelInfo)schema.getModel(row.getClass());
    if (model == null) {
      throw new DatabaseException("No model found associated with the given Row \"" + row.getClass() + "\"");
    }

    try {
      save(schema, model, row);
    } catch (SQLException e) {
      throw new DatabaseException("Error while trying to save a record", e);
    } catch (IllegalAccessException e) {
      throw new ModelException("Problem accessing row fields using reflection on " + row.getClass(), e);
    }


  }

  protected <T extends Row> void save(Schema schema, JDBCModelInfo<T> model, T row) throws SQLException, IllegalAccessException, DatabaseException {

    Model<T> m = model.getModel();

    // Check for reference types, if there are any records, with reference to
    // record on another table, need to save that first
    for(Map.Entry<String, Field> entry:model.getModel().getReferences()) {
      Reference refRow = (Reference)entry.getValue().get(row);
      if (refRow.getId() == null && refRow.get() != null) {
        save(schema, refRow.get());
      }
    }

    // If the model consists of parent model, checking on the ID will consider
    // the child model as already existent, so saving the isNew state on a
    // variable in the very beginning itself
    boolean isNew = row.getId() == null;

    // Save the parent model first
    if (model.getModel().hasParent()) {
      save(schema, (JDBCModelInfo)model.getModel().getParent().getMoreInfo(), row);
    }

    if (isNew) {
      // Do an insert
      PreparedStatement stmt = model.getInsertStatement();
      int index = prepareStatement(model, stmt, row);

      // if its a child model, the primary key is provided by the parent
      if (model.getModel().hasParent()) {
        stmt.setLong(index, row.getId());
        stmt.executeUpdate();
      } else {
        int keys = stmt.executeUpdate();
        if (keys > 0) {
          ResultSet rs = stmt.getGeneratedKeys();
          if (rs.next()) {
            m.updatePrimaryKey(row, rs.getLong(1));

            // Let's update the cache as well
            model.getModel().updateCache(row.getId(), row);
          }
        }
      }
    } else {
      PreparedStatement stmt = model.getUpdateStatement();
      int index = prepareStatement(model, stmt, row);

      stmt.setLong(index, row.getId());
      stmt.executeUpdate();
    }
  }

  protected <T extends Row> int  prepareStatement(JDBCModelInfo<T> model, PreparedStatement statement, Row row) throws SQLException, IllegalAccessException {
    statement.clearParameters();

    int index = 1;

    // Extract all the normal field values
    for (Map.Entry<String, Field> entry : model.getModel().getFields()) {
      statement.setObject(index++, entry.getValue().get(row));
    }

    // Extract all the reference field values
    for (Map.Entry<String, Field> entry : model.getModel().getReferences()) {
      Reference ref = (Reference)entry.getValue().get(row);
      if (ref.getId() == null) {
        statement.setNull(index++, Types.INTEGER);
      } else {
        statement.setLong(index++, ref.getId());
      }
    }

    return index;
  }

  protected String getAutoIncrementKeyword() {
    return "AUTOINCREMENT";
  }

  protected String getTypeName(Column column) {
    Class clazz = column.getType();

    String type;

    if (clazz == String.class) {
      type = "VARCHAR";
    } else if (clazz == Integer.class) {
      type = "INTEGER";
    } else if (clazz == Long.class) {
      type = "BIGINT";
    } else if (clazz == Boolean.class) {
      type = "BIT";
    } else if (clazz == Float.class) {
      type = "REAL";
    } else if (clazz == Double.class) {
      type = "DOUBLE";
    } else if (clazz == Date.class) {
      type = "TIMESTAMP";
    } else if (clazz == BigDecimal.class) {
      type = "NUMERIC";
    } else if (Row.class.isAssignableFrom(clazz)) {
      type = "INTEGER";
    } else if (clazz == byte[].class) {
      type = "BLOB";
    } else {
      throw new ModelException("Unknown column type " + clazz + " for " + column.getName());
    }

    if (column.getSize() > 0) {
      type += "(" + column.getSize() + ")";
    }

    return type;

  }

}
