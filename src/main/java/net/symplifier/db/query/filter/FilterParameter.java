package net.symplifier.db.query.filter;

import net.symplifier.db.Field;
import net.symplifier.db.query.Query;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by ranjan on 8/7/15.
 */
public class FilterParameter<T> implements FilterEntity {

  private T value;

  public FilterParameter() {
    this.value = null;
  }

  public FilterParameter(T value) {
    this.value = value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }


  @Override
  public void buildQuery(Query query, StringBuilder queryString) {
    queryString.append("?");

  }
}
