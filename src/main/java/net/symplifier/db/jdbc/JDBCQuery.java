package net.symplifier.db.jdbc;

import net.symplifier.db.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public class JDBCQuery<T extends Row> extends Query<T> {

  private String extractionSQL = null;
  private String orderSQL = null;
  private String limitSQL = null;

  private String preparedSql = null;
  private String preparedSqlCount = null;
  private PreparedStatement preparedStatement = null;
  private PreparedStatement countPreparedStatement = null;
  private List<Condition> preparedStatementValues = new ArrayList<>();

  public JDBCQuery(JDBCDriver driver, Model<T> primaryModel) {
    super(driver, primaryModel);

    extractionSQL = primaryModel.getName();
    extractionSQL += join(primaryModel);
  }

  private String join(Model model) {
    if (model.hasParent()) {
      return " INNER JOIN " + model.getParent().getName() + " ON "
              + model.getName() + "." + model.getPrimaryKeyFieldName()
              + "=" + model.getParent().getName() + "."
              + model.getParent().getPrimaryKeyFieldName()
              + join(model.getParent());
    } else {
      return "";
    }
  }

  @Override
  public void reset() {

    close();

    this.preparedSql = null;
    this.preparedSqlCount = null;
    this.preparedStatement = null;
    this.countPreparedStatement = null;
  }

  public void close() {
    try {
      if (preparedStatement != null) {
        preparedStatement.close();
      }

      if (countPreparedStatement != null) {
        countPreparedStatement.close();
      }
    } catch(SQLException e) {

    }
  }

  @Override
  public Query<T> clearLimit() {
    reset();
    this.limitSQL = null;
    return this;
  }

  @Override
  public Query<T> limit(int page, int pageSize) {
    reset();
    this.limitSQL = (page - 1)*pageSize + "," + pageSize;
    return this;
  }

  @Override
  public Query<T> clearOrderBy() {
    reset();
    this.orderSQL = null;
    return this;
  }

  @Override
  public Query<T> orderBy(Column column, boolean desc) {
    reset();
    //TODO: Make sure the column belongs to one of the tables
    Model model = primaryModel;

    if (this.orderSQL != null) {
      this.orderSQL += "," + findColumnName(column);
    } else {
      this.orderSQL = findColumnName(column);
    }

    if (desc) {
      this.orderSQL += " DESC";
    }

    return this;
  }


  @Override
  protected Query<T> createCopy() {
    JDBCQuery<T> q = new JDBCQuery<>((JDBCDriver)this.driver, this.primaryModel);
    q.extractionSQL = this.extractionSQL;
    q.limitSQL = this.limitSQL;
    q.orderSQL = this.orderSQL;

    return q;
  }

  private String findColumnName(Column column) {
    Model model = primaryModel;
    do {
      if (model.contains(column)) {
        return model.getName() + "." + column.getName();
      }
      model = model.getParent();
    } while (model != null);
    throw new ModelException("The column " + column.getName() + " does not belong to model " + primaryModel.getName() + " or any of its children");
  }

  private PreparedStatement getPreparedStatement(boolean count) throws DatabaseException {

    PreparedStatement stmt;
    if (count) {
      if (preparedSqlCount == null) {
        preparedSqlCount = generateSQL(true);
      }
      stmt = countPreparedStatement;
    } else {
      if (preparedSql == null) {
        preparedSql = generateSQL(false);
      }
      stmt = preparedStatement;
    }

    if (stmt == null) {
      JDBCDriver driver = (JDBCDriver)super.driver;
      String sql;
      if (count) {
        sql = "SELECT COUNT(*) FROM " + preparedSqlCount;
      } else {
        sql = "SELECT * FROM " + preparedSql;
      }
      try {
        stmt = driver.connection.prepareStatement(sql);
        if (count) {
          countPreparedStatement = stmt;
        } else {
          preparedStatement = stmt;
        }
      } catch(SQLException e) {
        throw new DatabaseException("Error while preparing SQL - " + sql, e);
      }
    }

    try {
      stmt.clearParameters();

      for (int i = 1; i <= preparedStatementValues.size(); ++i) {
        Object value = preparedStatementValues.get(i-1).getValue();
        if (value instanceof Row) {
          stmt.setLong(i, ((Row) value).getId());
        } else {
          stmt.setObject(i, preparedStatementValues.get(i - 1).getValue());
        }
      }
    } catch(SQLException e) {
      throw new DatabaseException("Error while generating query SQL", e);
    }
    return stmt;

  }

  public int getSize() throws DatabaseException {
    // Let's avoid the limit clause while do the counting
    PreparedStatement stmt = getPreparedStatement(true);

    try {
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      } else {
        return 0;
      }
    } catch(SQLException e) {
      throw new DatabaseException("Error while executing count sql", e);
    }
  }

  public RowIterator<T> getRows() throws DatabaseException {
    PreparedStatement stmt = getPreparedStatement(false);

    try {
      ResultSet rs = stmt.executeQuery();
      return new JDBCIterator<>(primaryModel, rs);
    } catch(SQLException e) {
      throw new DatabaseException("Error while executing query sql", e);
    }
  }

  private String filterToSQL(Filter filter) {
    String sql = " (";
    Iterator<FilterEntity> entities = filter.getEntities();
    while(entities.hasNext()) {
      FilterEntity entity = entities.next();
      if (entity == Filter.TRUE) {
        sql += " 1";
      } else if (entity == Filter.AND) {
        sql += " AND ";
      } else if (entity == Filter.OR) {
        sql += " OR ";
      } else if (entity == Filter.NOT) {
        sql += " NOT ";
      } else if (entity instanceof Filter) {
        sql += filterToSQL(filter);
      } else if (entity instanceof Condition) {
        Condition condition = (Condition) entity;
        Column column = condition.getColumn();
        Object value = condition.getValue();

        // Check if the column is valid
        sql += findColumnName(column);
        if (value == null) {
          switch (condition.getOperator()) {
            case Condition.IS:
              sql += " IS NULL";
              break;
            case Condition.IS_NOT:
              sql += " IS NOT NULL";
              break;
            default:
              throw new ModelException("Null data provided by invalid operator in query");
          }
        } else {
          switch (condition.getOperator()) {
            case Condition.GREATER_THAN:
              sql += ">?";
              break;
            case Condition.LESS_THAN:
              sql += "<?";
              break;
            case Condition.GREATER_THAN_OR_EQUALS:
              sql += ">=?";
              break;
            case Condition.LESS_THAN_OR_EQUALS:
              sql += "<=?";
              break;
            case Condition.IS:
              sql += "=?";
              break;
            case Condition.IS_NOT:
              sql += "<>?";
              break;
            case Condition.LIKE:
              sql += " LIKE ?";
              break;
            default:
              throw new ModelException("Unknown Operator in query ");
          }
          preparedStatementValues.add(condition);
        }
      }
    }

    sql += ")";
    return sql;
  }

  private String generateSQL(boolean count) {
    preparedStatementValues.clear();

    String sql = extractionSQL;
    Filter filter = getFilter();
    if (filter != null) {
      sql += " WHERE " + filterToSQL(filter);
    }

    // While doing the counting SQL, the ORDER BY clause and limit clause
    // do not have any affect, so ignore them
    if (!count) {
      if (orderSQL != null) {
        sql += " ORDER BY " + orderSQL;
      }

      if (limitSQL != null) {
        sql += " LIMIT " + limitSQL;
      }
    }

    return sql;
  }


  public String toString() {
    if (preparedStatement == null) {
      return "<not prepared yet>";
    } else {
      return preparedStatement.toString();
    }
  }
}
