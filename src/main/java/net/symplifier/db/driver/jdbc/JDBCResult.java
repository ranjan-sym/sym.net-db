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
    T res = null;

    Long primaryId = null;


    // update all the data on the cache as it is received
    // We receive a data from a number of different tables, we need to retrieve
    // on record for the primary model, so we might have more than one record
    // to fetch from the record set
    try {
      do {
        // We expect as many data as the number of fields that we have defined
        for (int i = 0; i < query.fields.length; ++i) {
          JDBCField field = query.fields[i];
          Column col = query.columns[i];
          Object value = field.get(resultSet, i + 1);

          // The chunk of data for different model is supposed to start with a
          // primary column.
          if (col.isPrimary()) {
            currentModel = col.getModel();
            currentRow = currentModel.getRow((Long) value);
            // We are probably dealing with the first record
            if (res == null) {
              primaryId = (Long) value;
              res = (T)primaryModel.create(currentRow);
            } else if (currentModel == primaryModel) {
              // Stop reading if the record set has moved to next row on the
              // primary model
              if (primaryId != value) {
                break;
              }
            }
          } else {
            // This assertion fails if the ordering of column in the models are
            // not correct or for some reasons, the primary key column is not
            // on the top of the column list
            assert (currentModel == col.getModel());
          }

          // if the columns are not ordered properly, we can get null pointer
          // exception here
          assert currentRow != null;
          currentRow.set(col.getIndex(), value);

        }
      } while (eof = resultSet.next());
    } catch(SQLException e) {
      throw new DatabaseException("Error while retrieving data from Query", e);
    }
    return res;
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
