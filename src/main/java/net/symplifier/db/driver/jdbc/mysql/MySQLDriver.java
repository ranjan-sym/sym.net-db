package net.symplifier.db.driver.jdbc.mysql;

import net.symplifier.db.Column;
import net.symplifier.db.ModelStructure;
import net.symplifier.db.Schema;
import net.symplifier.db.driver.jdbc.JDBCDriver;
import net.symplifier.db.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by ranjan on 8/17/15.
 */
public class MySQLDriver extends JDBCDriver {

  MySQLDriver(Schema schema, String uri, String username, String password) {
    super(schema, uri, username, password);
  }

  @Override
  public void createModel(ModelStructure structure) {
    StringBuilder builder = new StringBuilder();
    builder.append("CREATE TABLE IF NOT EXISTS ");
    builder.append(structure.getTableName());
    builder.append('(');
    for(int i=0; i<structure.getColumnCount(); ++i) {
      if (i > 0) {
        builder.append(',');
      }

      Column col = structure.getColumn(i);
      builder.append("\r\n\t");
      builder.append(col.getFieldName());
      builder.append(' ');
      builder.append(getTypeName(col.getValueType()));

      if (col.isPrimary()) {
        builder.append(" PRIMARY KEY");
      }
      if (col instanceof Column.Primary) {
        builder.append(" AUTO_INCREMENT");
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
            builder.append( ((Boolean)def ? 1 : 0) );
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
      builder = new StringBuilder();
      builder.append("CREATE ");
      if (index.isUnique()) {
        builder.append("UNIQUE ");
      }
      builder.append("INDEX IF NOT EXISTS IDX_");
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
}
