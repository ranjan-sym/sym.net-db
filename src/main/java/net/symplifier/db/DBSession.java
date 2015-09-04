package net.symplifier.db;

import java.util.*;

/**
 * The Database Session object that provides connection and is obtained via
 * Application session using
 * {@link net.symplifier.core.application.Session#get(Object, Class)} using
 * {@link Schema} object as the key and DBSession.class for the Class parameter
 *
 * Created by ranjan on 8/21/15.
 */
public abstract class DBSession {
  // A DB Session keeps track of all the changes that have been made in the
  // session and when the session is committed, these changes are used to
  // update the cache and handle the interceptors
  private final Set<ModelRow> insertedRows =new LinkedHashSet<>();
  private final Set<ModelRow> updatedRows = new LinkedHashSet<>();
  private final Set<ModelRow> deletedRows = new LinkedHashSet<>();
  private final Set<Model> updatedInstances = new LinkedHashSet<>();
  private final Set<Model> deletedInstances = new LinkedHashSet<>();

  /**
   * Prepares a query for execution to retrieve data from the database
   *
   * @param query The query object to be prepared
   * @param <T> The primary model of the Query
   * @return A prepared query, ready for execution with option to change parameters
   */
  public abstract <T extends Model> Query.Prepared<T> prepare(Query<T> query);


  private void clearAll() {
    insertedRows.clear();
    updatedRows.clear();
    deletedRows.clear();
  }

  public final void beginTransaction(Schema schema) {
    doBeginTransaction();
  }

  public abstract void doBeginTransaction();

  public final void rollbackTransaction(Schema schema) {
    doRollbackTransaction();
    // The easier of the implementations, we just need to forget everything
    clearAll();
  }

  public abstract void doRollbackTransaction();

  public final void commitTransaction(Schema schema) {
    doCommitTransaction();

    // Once the transaction is completed, we need to update the cache
    // First update all the inserted rows
    Iterator<ModelRow> iterator = insertedRows.iterator();
    while(iterator.hasNext()) {
      ModelRow insertedRow = iterator.next();
      // if this row has been deleted in the same session, no need
      // to anything
      if (deletedRows.contains(insertedRow)) {
        deletedRows.remove(insertedRow);
        iterator.remove();
      } else {
        insertedRow.getStructure().updateCache(insertedRow);
      }
    }

    // Next up the rows that have been updated
    iterator = updatedRows.iterator();
    while(iterator.hasNext()) {
      ModelRow updatedRow = iterator.next();
      // if this row has been deleted in the same session, no need
      // to update, as it will be deleted later
      if (!deletedRows.contains(updatedRow)) {
        updatedRow.getStructure().updateCache(updatedRow);
      } else {
        iterator.remove();
      }
    }

    // Finally remove the deleted rows from the cache
    for(ModelRow deletedRow:deletedRows) {
      deletedRow.getStructure().removeFromCache(deletedRow);
    }


    // Now fire the interceptors
    Iterator<Model> modelIterator;
    modelIterator = updatedInstances.iterator();
    while(modelIterator.hasNext()) {
      schema.fireUpdatedInterceptors(modelIterator.next());
    }

    modelIterator = deletedInstances.iterator();
    while(modelIterator.hasNext()) {
      schema.fireDeletedInterceptors(modelIterator.next());
    }

    clearAll();

  }

  public abstract void doCommitTransaction();
  /**
   * Inserts the row in the database
   * @param row A single row to be inserted
   */
  public final void insert(ModelRow row) {
    doInsert(row);

    // Once inserted, we must get the id as well
    insertedRows.add(row);
  }

  public abstract void doInsert(ModelRow row);

  /**
   * Updates the row in the database
   *
   * @param row A single row to be updated
   * @param id The primary key id of the row
   */
  public final void update(ModelRow row, long id) {
    // TODO The id field may not be needed here. Check the use cases and see if this could be removed
    doUpdate(row, id);

    updatedRows.add(row);
  }


  public abstract void doUpdate(ModelRow row, long id);

  /**
   * Method that is invoked by the ModelInstance after the instance
   * has been saved. This method makes sure that the interceptors are informed
   * once the model is committed into the database
   * @param model
   */
  public void saved(Model model) {
    this.updatedInstances.add(model);
  }

  /**
   * Method that is invoked by the ModelInstance after the instance has
   * been saved. This method makes sure that the interceptors are informed
   * once the model is committed into the database
   *
   * @param model
   */
  public void deleted(Model model) {
    this.deletedInstances.add(model);
  }

  /**
   * Deletes a row from the database
   *
   * @param row The model row that needs to be deleted
   */
  public final void delete(ModelRow row) {
    doDelete(row);

    deletedRows.add(row);
  }

  public abstract void doDelete(ModelRow row);

  /**
   * Updates the intermediate table (arising from HasMany join for Many-Many relationship)
   * @param intermediate The intermediate table
   * @param ref The HasMany Relation reference
   * @param refSource The source model as per HasMany relation ref
   * @param refTarget The target model as per HasMany relation ref
   */
  public final void updateIntermediate(ModelStructure intermediate,
                                       Relation.HasMany ref, Model refSource,
                                       Model refTarget) {
    doUpdateIntermediate(intermediate, ref, refSource, refTarget);
  }

  public abstract void doUpdateIntermediate(ModelStructure intermediate,
                                            Relation.HasMany ref, Model refSource,
                                            Model refTarget);

  /**
   * Delete a record from the intermediate table
   *
   * @param intermediate The intermediate table
   * @param ref The HasMany Relation reference
   * @param refSource The source model as per HasMany relation ref
   * @param refTarget The target model as per HasMany relation ref
   */
  public final void deleteIntermediate(ModelStructure intermediate,
                                          Relation.HasMany ref, Model refSource,
                                          Model refTarget) {
    doDeleteIntermediate(intermediate, ref, refSource, refTarget);
  }

  public abstract void doDeleteIntermediate(ModelStructure intermediate,
                                          Relation.HasMany ref, Model refSource,
                                          Model refTarget);
}
