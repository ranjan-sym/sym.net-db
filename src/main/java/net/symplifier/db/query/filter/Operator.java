package net.symplifier.db.query.filter;

import net.symplifier.db.query.Query;

/**
 * Created by ranjan on 7/29/15.
 */
public enum Operator implements FilterEntity {
  eq {
    @Override
    public String toString() {
      return "=";
    }
  },
  notEq {
    @Override
    public String toString() {
      return "<>";
    }
  },
  lessThan {
    @Override
    public String toString() {
      return "<";
    }
  },
  greaterThan {
    @Override
    public String toString() {
      return ">";
    }
  },
  lessThenEq {
    @Override
    public String toString() {
      return "<=";
    }
  },
  greaterThanEq {
    @Override
    public String toString() {
      return ">=";
    }
  },
  like {
    @Override
    public String toString() {
      return "LIKE";
    }
  },
  isNull {
    @Override
    public String toString() {
      return "IS NULL";
    }
  },
  notNull {
    @Override
    public String toString() {
      return "IS NOT NULL";
    }
  },

  not {
    @Override
    public String toString() {
      return "NOT";
    }
  },

  and {
    @Override
    public String toString() {
      return "AND";
    }
  },
  or {
    @Override
    public String toString() {
      return "OR";
    }
  };

  public void buildQuery(Query query, StringBuilder str) {
    str.append(query.getSchema().getDriver().getEntityText(this));
  }
}
