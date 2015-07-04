package net.symplifier.db.jdbc;

import net.symplifier.db.Column;
import net.symplifier.db.Driver;
import net.symplifier.db.Model;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

/**
 * Created by ranjan on 7/3/15.
 */
public class JDBCModelInfo extends Driver.ModelInfo {

  private final String insertSQL;
  private final String updateSQL;

  private PreparedStatement insertStatement;
  private PreparedStatement updateStatement;

  private final JDBCDriver driver;
  private final Model<?> model;

  public JDBCModelInfo(JDBCDriver driver, Model<?> model) {
    super(model);

    this.driver = driver;
    this.model = model;

    String table = model.getName();
    String insertSql = "INSERT INTO " + table + "(";
    String updateSql = "UPDATE " + table + " SET ";
    String insertSqlArgs = "";

    boolean first = true;

    for(int i=0; i<2; ++i) {
      Set<Map.Entry<String, Field>> columns;
      if (i == 0) {
        columns = model.getFields();
      } else {
        columns = model.getReferences();
      }
      for (Map.Entry<String, Field> entry: columns) {
        String columnName = entry.getKey();

        if (first) {
          first = false;
        } else {
          insertSql += ",";
          insertSqlArgs += ",";
          updateSql += ",";
        }

        insertSql += columnName;
        insertSqlArgs += "?";
        updateSql += columnName + "=?";
      }
    }

    if (model.hasParent()) {
      insertSql += "," + model.getPrimaryKeyFieldName();
      insertSqlArgs += ",?";
    }

    this.insertSQL = insertSql + ") VALUES (" + insertSqlArgs + ")";
    this.updateSQL = updateSql + " WHERE " + model.getPrimaryKeyFieldName() + "=?";
  }

  PreparedStatement getInsertStatement() throws SQLException {
    if (insertStatement == null) {
      if (model.hasParent()) {
        insertStatement = driver.connection.prepareStatement(insertSQL);
      } else {
        insertStatement = driver.connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
      }
    }

    return insertStatement;
  }

  PreparedStatement getUpdateStatement() throws SQLException {
    if (updateStatement == null) {
      updateStatement = driver.connection.prepareStatement(updateSQL);
    }

    return updateStatement;
  }


}
