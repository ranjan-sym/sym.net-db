package net.symplifier.db.filter;

/**
 * Created by ranjan on 7/29/15.
 */
public enum RelationalOperator implements Entity {
  eq,
  notEq,
  lessThan,
  greaterThan,
  lessThenEq,
  greaterThanEq,
  like,
  isNull,
  notNull
}
