package net.symplifier.db.driver.jdbc;

import net.symplifier.db.Model;
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
public class JDBCSession {
  private final Connection connection;

  // keep track of all the prepared statements that need to be closed when
  // the session ends

  private List<JDBCQuery.Prepared> preparedList = new ArrayList<>();

  public JDBCSession(Connection connection) {
    this.connection = connection;
  }

  public <T extends Model> JDBCQuery<T>.Prepared prepare(JDBCQuery<T> q) {
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

  public void close() {
    try {
      // Close all the prepared statements
      for(JDBCQuery.Prepared p:preparedList) {
        try {
          p.statement.close();
        } catch(SQLException e) {

        }
      }

      // Close all the result sets

      this.connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }



}
