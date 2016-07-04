package net.symplifier.db.driver.jdbc;

import net.symplifier.db.Column;
import net.symplifier.db.ModelStructure;
import net.symplifier.db.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ranjan on 8/22/15.
 */
public class SQLBuilder {
  private final StringBuilder builder;
  private final List<Query.Parameter> parameters;
  private final JDBCDriver driver;

  public SQLBuilder(JDBCDriver driver) {
    this.driver = driver;
    builder = new StringBuilder();
    parameters = new ArrayList<>();
  }

  public SQLBuilder append(String str) {
    builder.append(str);
    return this;
  }

  public SQLBuilder append(char c) {
    builder.append(c);
    return this;
  }

  public SQLBuilder append(int i) {
    builder.append(i);
    return this;
  }

  public SQLBuilder append(long l) {
    builder.append(l);
    return this;
  }

  public <T> SQLBuilder append(Column<?, T> column, T value) {
    builder.append(column.getFieldName());
    builder.append("=?");
    parameters.add(new Query.Parameter<T>(value).init(column));
    return this;
  }

  public SQLBuilder append(ModelStructure model) {
    builder.append(driver.quote(model.getTableName()));
    return this;
  }

  public SQLBuilder append(List<Column> columns, Object[] data, int offset) {
    StringBuilder values = new StringBuilder();
    builder.append('(');
    for(int i=offset; i<data.length; ++i) {
      Column col = columns.get(i);
      if (i>offset) {
        builder.append(',');
        values.append(',');
      }

      values.append('?');
      builder.append(driver.quote(col.getFieldName()));
      parameters.add(new Query.Parameter(data[i]).init(col));
    }
    builder.append(") VALUES (");
    builder.append(values);
    builder.append(')');
    return this;
  }

  public String getSQL() {
    return builder.toString();
  }

  public List<Query.Parameter> getParameters() {
    return parameters;
  }
}
