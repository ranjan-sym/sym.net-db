package net.symplifier.db;

import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public interface RowIterator<T extends Row> {

  boolean hasNext() throws DatabaseException;

  T next() throws DatabaseException;

  List<T> toList() throws DatabaseException;

}
