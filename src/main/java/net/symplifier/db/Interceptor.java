package net.symplifier.db;

/**
 * Created by ranjan on 8/14/15.
 */
public interface Interceptor {
  int INSERT = 1;
  int UPDATE = 2;
  int DELETE = 3;

  void onInsert(Class<? extends Model> model, long id);

  void onDelete(Class<? extends Model> model, long id);

  void onUpdate(Class<? extends Model> model, long id, Column column);
}
