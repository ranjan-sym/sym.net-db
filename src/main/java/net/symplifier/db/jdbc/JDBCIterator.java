package net.symplifier.db.jdbc;

import net.symplifier.db.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public class JDBCIterator<T extends Row> implements RowIterator<T> {

  private final ResultSet rs;
  private final Model<T> primaryModel;

  public JDBCIterator(Model<T> primaryModel, ResultSet rs) {
    this.rs = rs;
    this.primaryModel = primaryModel;
  }

  @Override
  public boolean hasNext() {
    try {
      return rs.next();
    } catch(SQLException e) {
      return false;
      //throw new DatabaseException("Error while iterating through resultset", e);
    }
  }

  @Override
  public List<T> toList() throws DatabaseException {
    List<T> list = new ArrayList<>();
    while(hasNext()) {
      list.add(next());
    }
    return list;
  }

  @Override
  public T next() {
    T row;
    try {
      long id = rs.getLong("id");
      if (!rs.wasNull()) {
        row = primaryModel.createRow(id);
        // The id of the row is not set if the createRow missed on a cache,
        // otherwise it hit on a cache, in which case, we will not load this
        // particular record from the RecordSet as it will pose a danger of
        // reverting back the changes being made to the row for editing purposes
        if (row.getId() != null && row.getId() == id) {
          return row;
        }
      } else {
        row = primaryModel.createRow();
      }
    } catch(SQLException e) {
      System.out.println("Unexpected error. We expect id in all the quries.");
      row = primaryModel.createRow();
    }

    try {
      ResultSetMetaData metaData = rs.getMetaData();
      for (int i = 1; i <= metaData.getColumnCount(); ++i) {
        String table = metaData.getTableName(i);

        Model<? extends Row> model = primaryModel;

        Field f;
        do {
          if (model.getName().equals(table)) {
            // Find out if the model has this column
            Column column = model.getColumn(metaData.getColumnName(i));
            // If the column is not found, try it out on the parent model
            if (column == null) {
              model = model.getParent();
              continue;
            }

            // We have to do the filter out here instead of inside model
            // because ResultSet.getObject would return Integer instead of
            // a Long in SQLITE, same kind of thing might happen in other
            // DB system, so as a safe bet, the pre pre-processing is done here
            // and make a direct updates through Model
            if (model.isPrimaryKey(column)) {
              model.updatePrimaryKey(row, rs.getLong(i));
            } else if ((f = model.isField(column)) != null) {
              Object value;
              if (column.getType() == Date.class) {
                // We need to convert timestamp to java.util.Date type
                value = rs.getTimestamp(i);
                if (value != null) {
                  value = new Date(((Timestamp) value).getTime());
                }
              } else if (column.getType() == Short.class) {
                value = rs.getShort(i);
              } else if (column.getType() == Double.class) {
                value = rs.getDouble(i);
              } else if (column.getType() == Long.class) {
                value = rs.getLong(i);
              } else if (column.getType() == Integer.class) {
                value = rs.getInt(i);
              } else if (column.getType() == String.class) {
                value = rs.getString(i);
              } else if (column.getType() == Float.class) {
                value = rs.getFloat(i);
              } else if (column.getType() == Boolean.class) {
                value = rs.getBoolean(i);
              } else if (column.getType() == Byte.class) {
                value = rs.getByte(i);
              } else if (column.getType() == byte[].class) {
                value = rs.getBytes(i);
              } else {
                value = rs.getObject(i);
              }
              if (rs.wasNull()) {
                model.updateField(row, f, column, null);
              } else {
                model.updateField(row, f, column, value);
              }
            } else if ((f = model.isReference(column)) != null) {
              model.updateReference(row, f, column, rs.getLong(i));
            } else {
              //TODO: A Warning message, no column found with the given name in the model
            }

            break;
          } else {
            model = model.getParent();
          }

        } while (model != null);

        if (model == null) {
          // TODO Warning message
        }
      }
    } catch (SQLException e) {
      return null;
      //throw new DatabaseException("Error while updating model from result set", e);
    }

    row.onLoaded();

    return row;
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }
}