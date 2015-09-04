package net.symplifier.db;

/**
 * Created by ranjan on 8/14/15.
 */
public interface Interceptor {
  int INSERT = 1;
  int UPDATE = 2;
  int DELETE = 4;
  int UPDATED = 8;
  int DELETED = 16;

  int getType();


  /**
   * An interceptor invoked before a record is inserted
   */
  interface Insert extends Interceptor {
    boolean onInsert(Schema schema, ModelRow row);

    default int getType() {
      return INSERT;
    }
  }

  /**
   * An interceptor invoked before a record is updated
   */
  interface Update extends Interceptor {
    boolean onUpdate(Schema schema, ModelRow row);

    default int getType() {
      return UPDATE;
    }

  }

  /**
   * An interceptor invoked before a record is deleted
   */
  interface Delete extends Interceptor {
    boolean onDelete(Schema schema, ModelRow row);

    default int getType() {
      return DELETE;
    }

  }

  /**
   * An interceptor invoked after a record is inserted/updated
   */
  interface Updated extends Interceptor {
    void onUpdated(Schema schema, Model model);

    default int getType() {
      return UPDATED;
    }

  }

  /**
   * An interceptor invoked after a record is Deleted
   */
  interface Deleted extends Interceptor {
    void onDeleted(Schema schema, Model model);

    default int getType() {
      return DELETED;
    }

  }

}
