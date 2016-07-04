package net.symplifier.db.driver.jdbc;

import net.symplifier.db.*;
import net.symplifier.db.exceptions.DatabaseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A JDBCSession responsible for providing connections on a per thread basis
 * and implement commit, rollback to updates the cache
 * Created by ranjan on 7/29/15.
 */
public class JDBCSession extends DBSession {
  public static final Logger LOGGER = LogManager.getLogger(JDBCSession.class);

  private final JDBCDriver driver;
  private final Connection connection;

  // keep track of all the prepared statements that need to be closed when
  // the session ends

  private List<JDBCQuery.Prepared> preparedList = new ArrayList<>();

  public JDBCSession(JDBCDriver driver, Connection connection) {
    this.driver = driver;
    this.connection = connection;
  }

  public Connection getConnection() {
    return connection;
  }

  public void doBeginTransaction() {
    try {
      this.connection.setAutoCommit(false);
    } catch (SQLException e) {
      LOGGER.error("Could not start transaction", e);
    }
  }

  public void doRollbackTransaction() {
    try {
      this.connection.rollback();
    } catch(SQLException e) {
      LOGGER.error("Could not rollback transaction", e);
    }
  }

  public void doCommitTransaction() {
    try {
      this.connection.commit();
    } catch(SQLException e) {
      LOGGER.error("Could not commit transaction", e);
    }
  }

  public void close() {
    try {
      // Close all the prepared statements
      for(JDBCQuery.Prepared p:preparedList) {
        try {
          p.statement.close();
        } catch(SQLException e) {
          LOGGER.error("Session closing. Error while closing prepared statement", e);
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
      String sql = q.getSQL();
      LOGGER.debug("Executing SQL - " + sql);
      PreparedStatement stmt = connection.prepareStatement(sql);
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
  public void doInsert(ModelRow row) {
    ModelStructure structure = row.getStructure();
    SQLBuilder sql = new SQLBuilder(driver);

    Long id = driver.doInsert(structure, row.getData());

    row.set(0, id);

    row.clearFlag();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void doUpdate(ModelRow row, long id) {
    ModelStructure structure = row.getStructure();
    SQLBuilder updateSql = new SQLBuilder(driver);

    updateSql.append("UPDATE ").append(structure);
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
      updateSql.append(col, row.get(i));
    }

    updateSql.append(" WHERE ");
    updateSql.append(structure.getColumn(0), row.get(0));

    String sql = updateSql.getSQL();
    System.out.println(sql);

    // hit the database if we find any column to update
    if(!first) {
      try(PreparedStatement statement = connection.prepareStatement(sql)) {
        List<Query.Parameter> parameters = updateSql.getParameters();
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
  public void doDelete(ModelRow row) {
    ModelStructure structure = row.getStructure();
    StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    sql.append(driver.quote(structure.getTableName()));
    sql.append(" WHERE ");
    sql.append(structure.getPrimaryKeyField());
    sql.append('=');
    sql.append(row.getId());

    try {
      connection.createStatement().execute(sql.toString());
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while trying to delete record", e);
    }
  }

  @Override
  public void doDeleteIntermediate(ModelStructure intermediate, Relation.HasMany ref, long sourceId, long childId) {
    StringBuilder sql = new StringBuilder();
    sql.append("DELETE FROM ");
    sql.append(driver.quote(intermediate.getTableName()));
    sql.append(" WHERE ");
    sql.append(driver.quote(ref.getSourceFieldName()));
    sql.append('=');
    sql.append(sourceId);
    sql.append(" AND ");
    sql.append(driver.quote(ref.getTargetFieldName()));
    sql.append('=');
    sql.append(childId);

    try {
      connection.createStatement().execute(sql.toString());
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while tyring to delete intermediate record", e);
    }
  }

  @Override
  public void doUpdateIntermediate(ModelStructure intermediate, Relation.HasMany ref, Model refSource, Model refTarget) {

    // Only insert a record if there isn't already a record there
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT * FROM ");
    sql.append(driver.quote(intermediate.getTableName()));
    sql.append(" WHERE ");
    sql.append(driver.quote(ref.getSourceFieldName()));
    sql.append("=");
    sql.append(refSource.getId());
    sql.append(" AND ");
    sql.append(driver.quote(ref.getTargetFieldName()));
    sql.append("=");
    sql.append(refTarget.getId());
    try {
      ResultSet rs = connection.createStatement().executeQuery(sql.toString());
      if (rs.next()) {
        rs.close();
        return;
      } else {
        rs.close();
      }
    } catch(SQLException e) {
      throw new DatabaseException("An error occurred while trying to fetch intermediate record for checking", e);
    }

    sql = new StringBuilder();
    sql.append("INSERT INTO ");
    sql.append(driver.quote(intermediate.getTableName()));
    sql.append('(');
    sql.append(driver.quote(ref.getSourceFieldName()));
    sql.append(',');
    sql.append(driver.quote(ref.getTargetFieldName()));
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
