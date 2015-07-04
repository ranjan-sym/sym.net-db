package net.symplifier.db;

import javax.xml.crypto.Data;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public interface Driver {

  /**
   * Mechanism for the Driver to keep cache of all the necessary tools required
   * for the model to run more efficiently. An example, might be preparing a
   * insertSQL or updateSQL during startup.
   *
   * @param model The Model for which more information needs to be pre-prepared
   * @return The new ModelInfo
   */
  ModelInfo generateModelInfo(Model model);

  /**
   * Begin a database transaction
   *
   * @throws DatabaseException
   */
  void begin() throws DatabaseException;

  /**
   * Commit the current database transaction
   *
   * @throws DatabaseException
   */
  void commit() throws DatabaseException;

  /**
   * Roll back the current database transaction
   *
   * @throws DatabaseException
   */
  void rollback() throws DatabaseException;

  /**
   * Create necessary structures in the database required to house the model's
   * records. This method must fall back safely if the structure is already
   * there.
   *
   * @param model The model that needs to be created.
   * @throws DatabaseException
   */
  void createModel(ModelInfo model) throws DatabaseException;

  /**
   * Saves the given Row of the Schema into the database
   *
   * @param schema The Schema to which the model belongs
   * @param row The row that needs to be saved.
   *
   * @throws DatabaseException
   */
  void save(Schema schema, Row row) throws DatabaseException;

  <T extends Row> Query<T> createQuery(Model<T> primaryModel);

  abstract class ModelInfo {

    private final Model model;

    public ModelInfo(Model model) {
      this.model = model;
    }

    public Model<?> getModel() {
      return model;
    }
  }

}
