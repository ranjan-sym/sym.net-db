package net.symplifier.db;

/**
 * Created by ranjan on 7/27/15.
 */
public interface Row {

  long getId();

  void setId(long id);

  boolean isNew();

  boolean isModified();

}
