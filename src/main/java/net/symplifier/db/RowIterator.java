package net.symplifier.db;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public interface RowIterator<T extends Row> extends Iterable<T>, Iterator<T>{

  boolean hasNext();

  T next();

  List<T> toList() throws DatabaseException;

}
