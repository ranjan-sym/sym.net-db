package net.symplifier.db.driver.jdbc;

import net.symplifier.db.*;
import net.symplifier.db.exceptions.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The wrapper for database result set for the Model
 *
 * Created by ranjan on 8/16/15.
 */
public class JDBCResult<T extends Model> implements Query.Result<T> {

  private final ResultSet resultSet;
  private final JDBCQuery<T> query;

  private boolean eof;

  public JDBCResult(JDBCQuery<T> query, ResultSet resultSet) {
    this.query = query;
    this.resultSet = resultSet;

    try {
      eof = resultSet.next();
    } catch(SQLException e) {
      throw new DatabaseException("Error while moving through SQL result", e);
    }
  }

  @Override
  public T next() {
    if (eof) {
      return null;
    }

    ModelStructure<T> primaryModel = query.getPrimaryModel();
    ModelStructure currentModel = primaryModel;
    ModelRow       currentRow = null;
    ModelInstance res = null;

    Long primaryId = null;
    JDBCQuery.ModelMap root = query.getModelMap();


    // update all the data on the cache as it is received
    // We receive a data from a number of different tables, we need to retrieve
    // on record for the primary model, so we might have more than one record
    // to fetch from the record set
    try {
      do {
        // We expect as many data as the number of fields that we have defined
        ModelInstance r = root.load(resultSet, res);
        if (r == null) {
          // Result set is at the next row
          break;
        } else {
          // We found a new row
          res = r;
        }
      } while (eof = resultSet.next());
    } catch(SQLException e) {
      throw new DatabaseException("Error while retrieving data from Query", e);
    }

    return (T)res;
  }


  @Override
  public List<T> toList() {
    List<T> res = new ArrayList<>();

    T rec;
    while((rec = next()) != null) {
      res.add(rec);
    }

    return res;
  }




}
