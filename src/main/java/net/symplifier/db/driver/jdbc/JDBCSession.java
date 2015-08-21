package net.symplifier.db.driver.jdbc;

import net.symplifier.db.*;
import net.symplifier.db.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JDBCSession responsible for providing connections on a per thread basis
 * and implement commit, rollback to updates the cache
 * Created by ranjan on 7/29/15.
 */
public class JDBCSession implements DBSession {
  private final JDBCDriver driver;
  private final Connection connection;

  // keep track of all the prepared statements that need to be closed when
  // the session ends

  private List<JDBCQuery.Prepared> preparedList = new ArrayList<>();

  public JDBCSession(JDBCDriver driver, Connection connection) {
    this.driver = driver;
    this.connection = connection;
  }

  public void close() {
    try {
      // Close all the prepared statements
      for(JDBCQuery.Prepared p:preparedList) {
        try {
          p.statement.close();
        } catch(SQLException e) {
          System.out.println("Error while closing statment");
          e.printStackTrace();
        }
      }

      // Close all the result sets

      this.connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  @Override
  public <T extends Model> JDBCQuery<T>.Prepared prepare(Query<T> query) {
    JDBCQuery<T> q = (JDBCQuery<T>) query;
    try {
      PreparedStatement stmt = connection.prepareStatement(q.getSQL());
      if (stmt == null) {
        return null;
      } else {
        JDBCQuery<T>.Prepared prepared = q.new Prepared(stmt);
        preparedList.add(prepared);
        return prepared;
      }
    } catch(SQLException e) {
      throw new DatabaseException("Error while preparing sql", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void insert(ModelRow row) {
    ModelStructure structure = row.getStructure();
    StringBuilder insertSql = new StringBuilder();
    StringBuilder valuesPart = new StringBuilder();
    List<Query.Parameter> parameters = new ArrayList<>();

    boolean needId = row.get(0) == null;

    insertSql.append("INSERT INTO ");
    insertSql.append(driver.formatFieldName(structure.getTableName()));
    insertSql.append('(');
    for(int i=0; i<structure.getColumnCount(); ++i){
      Column col = structure.getColumn(i);
      if (i > 0) {
        insertSql.append(',');
        valuesPart.append(',');
      }
      insertSql.append(driver.formatFieldName(col.getFieldName()));

      Object value = row.get(i);
      if(value == null) {
        valuesPart.append("NULL");
      } else {
        valuesPart.append('?');
        parameters.add(new Query.Parameter(value).init(col));
      }
    }

    insertSql.append(") VALUES (");
    insertSql.append(valuesPart);
    insertSql.append(')');


    String sql = insertSql.toString();

    try {
      PreparedStatement statement = connection.prepareStatement(sql);
      for (int i = 0; i < parameters.size(); ++i) {
        Query.Parameter parameter = parameters.get(i);
        JDBCParameter p = (JDBCParameter) parameter.getSetter();
        p.set(statement, i + 1, parameter.getDefault());
      }

      if (needId) {
        long id = 0;
        statement.executeQuery();
        row.set(0, id);
      } else {
        statement.executeUpdate();
      }
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while trying to insert record", e);
    }

    row.clearFlag();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void update(ModelRow row, long id) {
    ModelStructure structure = row.getStructure();
    StringBuilder updateSql = new StringBuilder();
    List<Query.Parameter> parameters = new ArrayList<>();

    updateSql.append("UPDATE ");
    updateSql.append(driver.formatFieldName(structure.getTableName()));
    updateSql.append(" SET ");
    boolean first = true;
    for(int i=1; i<structure.getColumnCount(); ++i) {
      // We will update only the modified field
      if (!row.isModified(i)) {
        continue;
      }
      Column col = structure.getColumn(i);
      // need to add a ',' after the first
      if (!first) {
        updateSql.append(',');
      } else {
        first = false;
      }
      updateSql.append(driver.formatFieldName(col.getFieldName()));
      updateSql.append('=');
      Object value = row.get(i);
      if (value == null) {
        updateSql.append("NULL");
      } else {
        updateSql.append('?');
        parameters.add(new Query.Parameter(value).init(col));
      }
    }

    String sql = updateSql.toString();

    // hit the database if we find any column to update
    if(!first) {
      try {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); ++i) {
          Query.Parameter parameter = parameters.get(i);
          JDBCParameter p = (JDBCParameter) parameter.getSetter();
          p.set(statement, i + 1, parameter.getDefault());
        }

        statement.executeUpdate();
      } catch (SQLException e) {
        throw new DatabaseException("An error occurred while trying to update record", e);
      }
    }

    // Update the primary key value any way
    row.set(0, id);

    // Clear the modification flag on this row
    row.clearFlag();
  }

  @Override
  public void delete(Model model, long id) {
    ModelStructure structure = model.getStructure();
    StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    sql.append(driver.formatFieldName(structure.getTableName()));
    sql.append(" WHERE ");
    sql.append(structure.getPrimaryKeyField());
    sql.append('=');
    sql.append(id);

    try {
      connection.createStatement().execute(sql.toString());
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while trying to delete record", e);
    }
  }

  @Override
  public void deleteIntermediate(ModelStructure intermediate, Relation.HasMany ref, Model refSource, Model refTarget) {
    StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    sql.append(driver.formatFieldName(intermediate.getTableName()));
    sql.append(" WHERE ");
    sql.append(driver.formatFieldName(ref.getSourceFieldName()));
    sql.append('=');
    sql.append(refSource.getId());
    sql.append(" AND ");
    sql.append(driver.formatFieldName(ref.getTargetFieldName()));
    sql.append('=');
    sql.append(refTarget.getId());

    try {
      connection.createStatement().execute(sql.toString());
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while tyring to delete intermediate record", e);
    }
  }

  @Override
  public void updateIntermediate(ModelStructure intermediate, Relation.HasMany ref, Model refSource, Model refTarget) {
    StringBuilder sql = new StringBuilder();
    sql.append("INSERT INTO ");
    sql.append(driver.formatFieldName(intermediate.getTableName()));
    sql.append('(');
    sql.append(driver.formatFieldName(ref.getSourceFieldName()));
    sql.append(',');
    sql.append(driver.formatFieldName(ref.getTargetFieldName()));
    sql.append(") VALUES (");
    sql.append(refSource.getId());
    sql.append(',');
    sql.append(refTarget.getId());
    sql.append(')');

    try {
      connection.createStatement().execute(sql.toString());
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while trying to update intermediate record", e);
    }
  }
}
