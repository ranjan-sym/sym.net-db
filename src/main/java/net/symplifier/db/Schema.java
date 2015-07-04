package net.symplifier.db;

import java.util.*;

/**
 * Created by ranjan on 7/3/15.
 */
public abstract class Schema {
  private final Driver driver;

  private final Map<String, Driver.ModelInfo> models = new LinkedHashMap<>();
  private final Map<Class<? extends Row>, Driver.ModelInfo> rowAssociatedModels = new HashMap<>();

  public Schema(Driver driver) {
    this.driver = driver;
  }

  public void begin() throws DatabaseException {
    driver.begin();
  }

  public void commit() throws DatabaseException {
    driver.commit();
  }

  public void rollback() throws DatabaseException {
    driver.rollback();
  }


  public void createAll() throws DatabaseException {
    for(Driver.ModelInfo model:models.values()) {
      driver.createModel(model);
    }
  }

  public void save(Row row) throws DatabaseException {
    driver.save(this, row);
  }

  public <T extends Row> Query<T> createQuery(Model<T> primaryModel) {
    return driver.createQuery(primaryModel);
  }

  public Driver.ModelInfo getModel(Class<? extends Row> rowClass) {
    return rowAssociatedModels.get(rowClass);
  }

  public boolean hasModel(String name) {
    return models.containsKey(name);
  }

  public Driver.ModelInfo registerModel(String name, Model<?> model) {
    assert(!models.containsKey(name));
    Driver.ModelInfo info = driver.generateModelInfo(model);
    models.put(name, info);
    rowAssociatedModels.put(model.getAssociatedRow(), info);

    return info;
  }
}
