package net.symplifier.db;

/**
 * Created by ranjan on 8/14/15.
 */
public interface Interceptor {
  int INSERT = 1;
  int UPDATE = 2;
  int DELETE = 4;

  void onInsert(Schema schema, Class<? extends Model> model, long id);

  void onDelete(Schema schema, Class<? extends Model> model, long id);

  void onUpdate(Schema schema, Class<? extends Model> model, long id);
}
