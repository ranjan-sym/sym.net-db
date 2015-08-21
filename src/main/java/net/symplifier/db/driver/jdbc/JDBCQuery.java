package net.symplifier.db.driver.jdbc;

import net.symplifier.core.application.Session;
import net.symplifier.db.*;
import net.symplifier.db.exceptions.DatabaseException;

import java.sql.*;
import java.util.*;

/**
 * The Query implementation for JDBC
 *
 * Created by ranjan on 7/29/15.
 */
public class JDBCQuery<M extends Model> implements Query<M> {

  private class Alias<T extends Model> {
    private final ModelStructure<T> model;
    private final Filter<T> filter;
    private final String name;
    private final Set<Order<T>> orders;

    public Alias(ModelStructure<T> model, Filter<T> filter, Set<Order<T>> orders) {
      this.model = model;
      this.filter = filter;
      this.name = "A" + (++aliasNumber);
      this.orders = orders;

      aliases.add(this);
    }

    public ModelStructure<T> getModel() {
      return model;
    }

    public Filter<T> getFilter() {
      return filter;
    }

    public Set<Order<T>> getOrders() {
      return orders;
    }

    public String toString() {
      return name;
    }
  }

  private static final EnumMap<FilterOp, String> filterOperations = new EnumMap<FilterOp, String>(FilterOp.class) {{
    this.put(FilterOp.and, " AND ");
    this.put(FilterOp.or, " OR ");
    this.put(FilterOp.eq, "=");
    this.put(FilterOp.notEq, "<>");
    this.put(FilterOp.lt, "<");
    this.put(FilterOp.ltEq, "<=");
    this.put(FilterOp.gt, ">");
    this.put(FilterOp.gtEq, ">=");
    this.put(FilterOp.isNull, " IS NULL");
    this.put(FilterOp.isNotNull, " IS NOT NULL");
    this.put(FilterOp.in, " IN");
    this.put(FilterOp.like, " LIKE ");
  }};

  private final JDBCDriver driver;
  private final ModelStructure<M> primaryModel;
  private final List<Alias> aliases = new ArrayList<>();
  private final List<Parameter> parameters = new ArrayList<>();

  final JDBCField[] fields;
  final Column[] columns;

  private final String sql;

  private int aliasNumber = 0;

  public JDBCQuery(JDBCDriver driver, Query.Builder<M> builder) {
    this.driver = driver;
    this.primaryModel = builder.getPrimaryModel();
    StringBuilder sqlBuffer = new StringBuilder();

    Set<Order<M>> orders = builder.getOrderBy();
    Alias alias = new Alias(primaryModel, builder.getFilter(), orders);

    Query.Limit limit = builder.getLimit();
    // Limit is tricky, when used with JOIN
    sqlBuffer.append("\r\n  FROM ");
    if (builder.getJoins().size() > 0 && limit != null) {
      // We have joins, the limit has to be treated a bit specially
      sqlBuffer.append("(SELECT * FROM ");
      sqlBuffer.append(builder.getPrimaryModel().getTableName());
      buildLimit(sqlBuffer, parameters, limit, true);
      sqlBuffer.append(')');
      limit = null;
    } else {
      sqlBuffer.append(builder.getPrimaryModel().getTableName());
    }
    sqlBuffer.append(" AS ");
    sqlBuffer.append(alias);


//    Plan to implement parent level join with the help of primary keys
//    // Join all the parents as well
//    for(ModelStructure parent:primaryModel.getParents()) {
//      makeJoin(sqlBuffer, "INNER", parent.getTableName(), alias, primaryModel.getPrimaryKeyField(), new Alias(parent, null), parent.getPrimaryKeyField());
//    }

    // Join all other references
    buildJoins(sqlBuffer, alias, builder.getPrimaryModel(), builder.getJoins());

    // Now prepare the filter
    buildFilter(sqlBuffer, parameters);

    // Next up is the order by
    builderOrderBy(sqlBuffer);

    // if we still have a limit to process, we need to process it here now
    if (limit != null) {
      buildLimit(sqlBuffer, parameters, limit, false);
    }

    StringBuilder columnNames = new StringBuilder();
    columnNames.append("SELECT ");
    // count total number of columns
    int totalColumns = 0;
    for(Alias a:aliases) {
      totalColumns += a.getModel().getColumnCount();
    }
    fields = new JDBCField[totalColumns];
    columns = new Column[totalColumns];


    // get the list of the columns that we need to fetch
    int c = 0;
    for(Alias a:aliases) {
      ModelStructure model = a.getModel();
      for(int i=0; i<model.getColumnCount(); ++i) {
        if (c>0) {
          columnNames.append(", ");
          if (i == 0) {   // Just making the URL pretty
            columnNames.append("\r\n       ");
          }
        }
        columnNames.append(a.toString());
        columnNames.append('.');
        columnNames.append(model.getColumn(i).getFieldName());

        Column col = model.getColumn(i);
        columns[c] = col;
        fields[c++] = (JDBCField)col.getField();
      }
    }

    columnNames.append(sqlBuffer);

    sql = columnNames.toString();
  }

  public ModelStructure<M> getPrimaryModel() {
    return primaryModel;
  }

  public String getSQL() {
    return sql;
  }

  public class Prepared implements Query.Prepared<M> {

    final PreparedStatement statement;
    private final Map<Parameter, Object> values = new HashMap<>();

    Prepared(PreparedStatement stmt) {
      this.statement = stmt;
    }

    public <T> Prepared set(Parameter<T> parameter, T value) {
      values.put(parameter, value);
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result<M> execute() {
      try {
        for (int i = 0; i < parameters.size(); ++i) {
          Parameter p = parameters.get(i);
          Object v = values.get(p);
          if (v == null) {
            v = p.getDefault();
          }


          ((JDBCParameter) p.getSetter()).set(statement, i + 1, v);
        }

        ResultSet rs = statement.executeQuery();
        return new JDBCResult<>(JDBCQuery.this, rs);
      } catch(SQLException e) {
        throw new DatabaseException("Error while executing sql", e);
      }

    }
  }

  public Result<M> execute(DBSession session) {
    return prepare(session).execute();
  }

  @Override
  public <V> Query.Prepared<M> set(Parameter<V> parameter, V value) {
    return prepare().set(parameter, value);
  }

  @Override
  public Query.Prepared<M> prepare(DBSession session) {
    return session.prepare(this);
  }

  @SuppressWarnings("unchecked")
  protected void buildFilter(StringBuilder sqlBuffer, List<Parameter> parameters) {
    boolean prefixed = false;
    boolean appended = false;
    for (Alias alias : aliases) {
      Filter filter = alias.getFilter();

      if (filter == null) {
        continue;
      }

      if (appended) {
        sqlBuffer.append("\r\n    AND ");
      }

      if (!prefixed && filter.getOperationCount() > 0) {
        sqlBuffer.append("\r\n  WHERE ");
        prefixed = true;
      }

      int checkLen = sqlBuffer.length();
      generateFilterQuery(sqlBuffer, parameters, alias, filter);
      appended = sqlBuffer.length() > checkLen;
    }
  }

  @SuppressWarnings("unchecked")
  protected void generateFilterQuery(StringBuilder sqlBuffer, List<Parameter> parameters, Alias alias, Filter filter) {
    List<FilterEntity> entities = filter.getEntities();
    for(FilterEntity entity:entities) {
      if (entity instanceof FilterOp) {
        sqlBuffer.append(filterOperations.get(entity));
      } else if (entity instanceof Column) {
        sqlBuffer.append(alias.toString());
        sqlBuffer.append('.');
        sqlBuffer.append(driver.formatFieldName(((Column) entity).getFieldName()));
      } else if (entity instanceof Parameter) {
        sqlBuffer.append('?');
        //JDBCParameter.Factory factory = (JDBCParameter.Factory)((Parameter) entity).getColumn().getParameterFactory();
        parameters.add((Parameter) entity);
        //parameters.put((Parameter) entity, factory.createModel(parameters.size(), (Parameter) entity));
      } else if (entity instanceof ParameterList) {
        //JDBCParameter.Factory factory = (JDBCParameter.Factory)((ParameterList) entity).getColumn().getParameterFactory();
        ParameterList list = (ParameterList) entity;
        sqlBuffer.append('(');
        for (int i = 0; i < list.getParameters().size(); ++i) {
          Parameter p = (Parameter) list.getParameters().get(i);
          if (i > 0) {
            sqlBuffer.append(',');
          }
          sqlBuffer.append('?');
          parameters.add(p);
          //parameters.put(p, factory.createModel(parameters.size(), p));
        }
      } else if (entity instanceof Filter) {
        Filter f = (Filter) entity;
        int opCount = f.getOperationCount();
        if (opCount > 1) {
          sqlBuffer.append('(');
          generateFilterQuery(sqlBuffer, parameters, alias, f);
          sqlBuffer.append(')');
        } else {
          generateFilterQuery(sqlBuffer, parameters, alias, f);
        }
      }
    }
  }

  protected void builderOrderBy(StringBuilder sqlBuffer) {
    sqlBuffer.append("\r\n\tORDER BY ");
    boolean first = true;
    for(Alias alias:aliases) {
      if (!first) {
        sqlBuffer.append(",\r\n           ");
        first = true;
      }
      Set<Order<?>> orders = alias.getOrders();
      if (orders.size() == 0) {
        // If no order by has been defined, then the primary key is taken
        // as the default ordering
        sqlBuffer.append(alias.toString());
        sqlBuffer.append('.');
        sqlBuffer.append(alias.getModel().getPrimaryKeyField());
        first = false;
      } else {
        for(Order order:orders) {
          if (!first) {
            sqlBuffer.append(", ");
          } else {
            first = false;
          }
          Column col = order.getColumn();
          sqlBuffer.append(alias.toString());
          sqlBuffer.append('.');
          sqlBuffer.append(col.getFieldName());

          if (order.isDescending()) {
            sqlBuffer.append(" DESC");
          }
        }
      }

    }
  }

  protected void buildLimit(StringBuilder sqlBuffer, List<Parameter> parameters, Query.Limit limit, boolean inline) {
    assert(limit != null);
    if (inline) {
      sqlBuffer.append(' ');
    } else {
      sqlBuffer.append("\r\n\t");
    }
    sqlBuffer.append("LIMIT ");
    Parameter<Integer> offset = limit.getOffset();
    Parameter<Integer> count = limit.getLimit();
    if (offset == null) {
      sqlBuffer.append('?');
      parameters.add(count);
    } else {
      sqlBuffer.append("?, ?");
      parameters.add(offset);
      parameters.add(count);
    }
  }

  private void makeJoin(StringBuilder sqlBuffer, String joinType, String joinTable, String parentAlias, String parentField, String childAlias, String childField) {
    sqlBuffer.append("\r\n    ");
    sqlBuffer.append(joinType);
    sqlBuffer.append(" JOIN ");
    sqlBuffer.append(joinTable);
    sqlBuffer.append(" AS ");
    sqlBuffer.append(childAlias);
    sqlBuffer.append(" ON ");
    sqlBuffer.append(parentAlias);
    sqlBuffer.append('.');
    sqlBuffer.append(parentField);
    sqlBuffer.append('=');
    sqlBuffer.append(childAlias);
    sqlBuffer.append('.');
    sqlBuffer.append(childField);
  }

  @SuppressWarnings("unchecked")
  private void buildJoins(StringBuilder sqlBuffer, Alias parentAlias, ModelStructure parent, List<Query.Join> joins) {

    for(Query.Join join:joins) {

      Reference reference = join.getReference();

      Alias joinAlias = new Alias(reference.getTargetType(), join.filter(), join.getOrderBy());
      // If there is an intermediate table in the join then there are actually two joins to be made
      ModelStructure<ModelIntermediate> intermediate = reference.getIntermediateTable();
      if (intermediate != null) {
        //Alias alias = new Alias(intermediate, null);
        String alias = "I" + (++aliasNumber);
        makeJoin(sqlBuffer, "LEFT", intermediate.getTableName(), parentAlias.toString(), parent.getPrimaryKeyField(), alias, reference.getSourceFieldName());
        //joinAlias = new Alias(reference.getTargetType(), join.filter());
        makeJoin(sqlBuffer, "LEFT", reference.getTargetType().getTableName(), alias, reference.getTargetFieldName(), joinAlias.toString(), reference.getTargetType().getPrimaryKeyField());
      } else {
        //joinAlias = new Alias(reference.getTargetType(), join.filter());
        makeJoin(sqlBuffer, "LEFT", reference.getTargetType().getTableName(), parentAlias.toString(), reference.getSourceFieldName(), joinAlias.toString(), reference.getTargetFieldName());
      }


      buildJoins(sqlBuffer, joinAlias, reference.getTargetType(), join.getJoinChildren());

    }
  }

  public String toString() {
    return sql;
  }

}
