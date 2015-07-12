package net.symplifier.db;

import java.util.*;

/**
 * Created by ranjan on 7/3/15.
 */
public abstract class Schema {
  private final Driver driver;

  private final Map<String, Driver.ModelInfo> models = new LinkedHashMap<>();
  private final Map<Class<? extends Row>, Driver.ModelInfo<? extends Row>> rowAssociatedModels = new HashMap<>();

  public Schema(Driver driver) {
    this.driver = driver;
  }

  public Model<?> findModel(String name) {
    Driver.ModelInfo info = models.get(name);
    if (info != null) {
      return info.getModel();
    }
    return null;
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

  public <T extends Row> T createRow(Class<T> clazz) {
    Driver.ModelInfo<T> model = getModel(clazz);
    return model.getModel().createRow();
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

  public <T extends Row> Driver.ModelInfo<T> getModel(Class<T> rowClass) {
    return (Driver.ModelInfo<T>)rowAssociatedModels.get(rowClass);
  }

  public boolean hasModel(String name) {
    return models.containsKey(name);
  }

  public <T extends Row> Driver.ModelInfo<T> registerModel(String name, Model<T> model) {
    assert(!models.containsKey(name));
    Driver.ModelInfo<T> info = driver.generateModelInfo(model);
    models.put(name, info);
    rowAssociatedModels.put(model.getAssociatedRow(), info);

    return info;
  }
}
