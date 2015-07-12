package net.symplifier.db;

import java.util.PriorityQueue;
import java.util.TreeSet;

/**
 * Created by ranjan on 7/10/15.
 */
public interface SequencedRow <T extends SequencedRow> {

  Long getId();

  void setId(Long id);

  Integer getSeq();

  void setSeq(Integer seq);

  TreeSet<T> getContainer();

}
