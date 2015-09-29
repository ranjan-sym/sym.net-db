package net.symplifier.db.goodies;

import net.symplifier.db.Schema;

import java.util.HashMap;

/**
 * Created by ranjan on 9/27/15.
 */
public class Oracle {
  private final static Oracle oracle = new Oracle();

  public static Oracle get() {
    return oracle;
  }

  private final HashMap<String, String> metaDataCache = new HashMap<>();

  public synchronized String getMetaData(String name) {
    if (metaDataCache.containsKey(name)) {
      return metaDataCache.get(name);
    } else {
      String metaData = Schema.get().getModelStructure(name).getMetaData().toString();
      metaDataCache.put(name, metaData);
      return metaData;
    }
  }

  public String records(String table) {
    return Schema.get().getModelStructure(table).query().build().execute().toJSON().toString();
  }
}
