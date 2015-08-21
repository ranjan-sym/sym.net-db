package net.symplifier.db;

/**
 * The Database Session object that provides connection and is obtained via
 * Application session using
 * {@link net.symplifier.core.application.Session#get(Object, Class)} using
 * {@link Schema} object as the key and DBSession.class for the Class parameter
 *
 * Created by ranjan on 8/21/15.
 */
public interface DBSession {

  /**
   * Prepares a query for execution to retrieve data from the database
   *
   * @param query The query object to be prepared
   * @param <T> The primary model of the Query
   * @return A prepared query, ready for execution with option to change parameters
   */
  <T extends Model> Query.Prepared<T> prepare(Query<T> query);

  /**
   * Inserts the row in the database
   * @param row A single row to be inserted
   */
  void insert(ModelRow row);

  /**
   * Updates the row in the database
   *
   * @param row A single row to be updated
   * @param id The primary key id of the row
   */
  void update(ModelRow row, long id);

  /**
   * Deletes a row from the database
   *
   * @param model The model that needs to be deleted
   * @param id The primary key id of the row
   */
  void delete(Model model, long id);

  /**
   * Updates the intermediate table (arising from HasMany join for Many-Many relationship)
   * @param intermediate The intermediate table
   * @param ref The HasMany Relation reference
   * @param refSource The source model as per HasMany relation ref
   * @param refTarget The target model as per HasMany relation ref
   */
  void updateIntermediate(ModelStructure intermediate, Relation.HasMany ref, Model refSource, Model refTarget);

  /**
   * Delete a record from the intermediate table
   *
   * @param intermediate The intermediate table
   * @param ref The HasMany Relation reference
   * @param refSource The source model as per HasMany relation ref
   * @param refTarget The target model as per HasMany relation ref
   */
  void deleteIntermediate(ModelStructure intermediate, Relation.HasMany ref, Model refSource, Model refTarget);
}
