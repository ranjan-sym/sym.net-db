package net.symplifier.db;

import net.symplifier.db.exceptions.ModelException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * An instance of a model that consists (or points) to the data represented by
 * a record of this Model. In other terms, it consists of a set of ModelRow
 *
 * Created by ranjan on 7/28/15.
 */
public class ModelInstance<M extends ModelInstance> implements Model {



  public class ModelSet {
    private final ModelStructure<M> structure;
    private final ModelRow[] allRows;
    /* A reference to the row in the all Rows that point to the primary Row */
    private final ModelRow primaryRow;

    public ModelSet(ModelStructure<M> structure, ModelRow primaryRow) {
      this.structure = structure;

      this.primaryRow = primaryRow;
      allRows = new ModelRow[structure.getDependentTablesCount()];
      allRows[structure.getModelLevel()] = primaryRow;
    }
  }

  private ModelSet set;
  private volatile boolean locked;

  /**
   * Constructor used to create an instance of the Model. A newly created
   * instance doesn't contain any data and is not associated with a Schema.
   * This default instantiation creates a new and fresh record ready for
   * populating with new data. Once created by this, it could either be used
   * as a new record, or initialized further by the following two methods
   * {@link }
   */
  public ModelInstance() {

  }

  @Override
  public void lock() {
    this.locked = true;
  }

  @Override
  public void unlock() {
    this.locked = false;
  }

  /**
   * A model created from an existing row
   *
   * @param primaryRow
   */
  public void init(ModelRow primaryRow) {
    assert(primaryRow.getStructure().getType() == this.getClass());
    set = new ModelSet(primaryRow.getStructure(), primaryRow);
  }

  /**
   * A new model created through a schema
   * @return
   */
  public void init(Schema schema) {
    ModelStructure s = schema.getModelStructure(getClass());
    set = new ModelSet(s, new ModelRow(s));
  }

  @Override
  public ModelStructure<M> getStructure() {
    if (set == null) {
      return Schema.get().getModelStructure(getClass());
    } else {
      return set.primaryRow.getStructure();
    }
  }

  public Long getId() {
    if (set == null) {
      return null;
    } else {
      return set.primaryRow.getId();
    }
  }

  public ModelRow getPrimaryRow() {
    if (set == null) {
      return null;
    } else {
      return set.primaryRow;
    }
  }

  @Override
  public <T> T get(Column<?, T> column, int level) {
    if (set == null) {
      return null;
    } else {
      ModelRow row = set.allRows[level];
      if (row == null) {
        // Looks like the row has not been loaded yet, time to load the row
        ModelStructure s = column.getModel();
        if (s.isInterface()) {
          // If the column that we are trying to retrieve is from an implementation
          // then we need to figure out the id of the implemented row and retrieve
          // the record
          Column.Interface interfaceColumn = getStructure().getImplementationColumn(s);
          Long implId = (Long)get((Column)interfaceColumn);
          row = set.allRows[level] = s.getRow(implId);
          return (T)row.get(column.getIndex());
        } else {
          // if this is a new record then we know the value is not there
          Long id = getId();
          if (id == null) {
            return null;
          } else {
            row = set.allRows[level] = s.getRow(id);
          }
          return (T)row.get(column.getIndex());
        }
      } else {
        return (T)row.get(column.getIndex());
      }
    }
  }

  private void checkLock() {
    if (locked) {
      throw new ModelException(this.getStructure().getType(), "Trying to change property of a locked model");
    }
  }

  /**
   * Set's the parent row item while loading data
   *
   * @param l the level of the parent
   * @param parentRow the row that is being loaded
   */
  public void setParentRow(int l, ModelRow parentRow) {
    assert(set.allRows[l] == null);
    set.allRows[l] = parentRow;
  }

  @Override
  public <T> void set(Column<?, T> column, int level, T value) {
    checkLock();


    ModelRow row;
    if (set == null) {
      init(Schema.get());
    }

    row = set.allRows[level];
    if (row == null) {
      row = set.allRows[level] = new ModelRow(column.getModel());
    }

    int colIndex = column.getIndex();
    Object chk = row.get(colIndex);

    // Let's first see if its actually a change
    if (value == chk || (value != null && value.equals(chk))) {
      return;
    }

    // If we are modifying a new row, we will createModel a copy
    // and work on that
    if (!row.isModified()) {
      row = set.allRows[level] = row.copy();
    }

    // finally change the value and mark it dirty
    row.set(colIndex, value, true);
  }

  public boolean isModified() {
    for(ModelRow r:set.allRows) {
      if(r.isModified()) {
        return true;
      }
    }
    return false;
  }

  private class RelationalData<V extends Model> {
    private final Map<Long, V> existingRecords;
    private final List<V> newRecords;

    private RelationalData() {
      existingRecords = new LinkedHashMap<>();
      newRecords = new ArrayList<>();
    }

    public void clear() {
      existingRecords.clear();
      newRecords.clear();
    }

    /**
     * Get method for HasMany relation
     *
     * @return
     */
    public List<V> getAll() {
      List<V> res = new ArrayList<>(existingRecords.size() + newRecords.size());

      res.addAll(existingRecords.values());
      res.addAll(newRecords);

      return res;
    }

    /**
     * Set method for HasMany relation
     *
     * @param record
     */
    public void add(V record) {
      checkLock();

      Long id = record.getId();
      if (id == null) {
        newRecords.add(record);
      } else {
        existingRecords.put(id, record);
      }
    }
  }

  private final Map<Relation.HasMany, RelationalData> hasManyData = new LinkedHashMap<>();
  private final Map<Column.Reference, Model> referencedData = new LinkedHashMap<>();


  /**
   * Clears all relational data
   */
  public void clearAll() {
    checkLock();

    hasManyData.clear();
    referencedData.clear();
  }

  /**
   * Clears the data for specific relation only
   *
   * @param relation The relation which needs to be cleared
   * @param <U> The relationship owner model type
   * @param <V> The relationship target model type
   */
  public <U extends M, V extends Model> void clear(Reference<U, V> relation) {
    checkLock();

    hasManyData.remove(relation);
    referencedData.remove(relation);
  }

  /**
   * Retrieve all the child data given the relationship token
   *
   * <p>
   *   Note that this method first checks the cache for available data and returns
   *   the cached data if available otherwise returns the data provided by
   *   {@link #loadAll(Relation.HasMany)}. The cached data may not be the complete
   *   set but a result of a filter operation either through an initial query
   *   or because of a manual filter application during data retrieval. Besides,
   *   the list will also not reflect any data added somewhere else in the code.
   * </p>
   *
   * @param relation The relationship token for which the data needs to be retrieved
   *
   * @param <U> The owner model type
   * @param <V> The target model type
   * @return List of all the model record
   */
  public <U extends M, V extends Model> List<V> get(Relation.HasMany<U, V> relation) {
    if (hasManyData.containsKey(relation)) {
      return (List<V>)hasManyData.get(relation).getAll();

    } else {
      return loadAll(relation);
    }
  }

  @Override
  public <U extends Model, V extends Model> V getReference(Column.Reference<U, V> column) {
    if (referencedData.containsKey(column)) {
      return (V)referencedData.get(column);
    } else {
      V res = null;
      Long id = get((Column<U, Long>) column);
      if (id != null) {
        res = column.getTargetType().get(id);
        referencedData.put(column, res);
      }
      return res;
    }
  }

  @Override
  public <U extends Model, V extends Model> void setReference(Column.Reference<U, V> column, V reference) {
    referencedData.put(column, reference);

    if (reference == null || reference.getId() == null) {
      set(column, (Long)null);
    } else {
      set(column, reference.getId());
    }
  }

  /**
   * Helper method used by query loading while setting the referenced row
   *
   * @param ref
   * @param id
   * @return
   */
  public ModelInstance get(Reference ref, long id) {
    if (ref instanceof Column.Reference) {
      Model m = referencedData.get(ref);
      if (m == null || m.getId() != id) {
        return null;
      } else {
        return (ModelInstance)m;
      }
    } else if (ref instanceof Relation.HasMany) {
      RelationalData d = hasManyData.get(ref);
      if (d == null || !d.existingRecords.containsKey(id)) {
        return null;
      } else {
        return (ModelInstance)d.existingRecords.get(id);
      }
    } else {
      return null;
    }
  }

  /**
   * Helper method used by query loading while setting the referenced row
   * @param ref
   * @param id
   * @param model
   */
  public void set(Reference ref, long id, Model model) {
    if (ref instanceof Column.Reference) {
      referencedData.put((Column.Reference)ref, model);
    } else if (ref instanceof Relation.HasMany) {
      RelationalData d = hasManyData.get(ref);
      d.add(model);
    }
  }

  /**
   * Load all the data for the relationship, cache and return
   *
   * @param relation The relation for which data needs to be retrieved
   * @param <U> The owner model type
   * @param <V> The target model type
   * @return List of all the model
   */
  public <U extends M, V extends Model> List<V> loadAll(Relation.HasMany<U, V> relation) {
    return filter(relation, new Query.Filter<V>());
  }

  /**
   * Load all the data for the relationship with filter, cache and return
   *
   * <p>
   *
   * </p>
   * @param relation
   * @param filter
   * @param <U>
   * @param <V>
   * @return
   */
  public <U extends M, V extends Model> List<V> filter(Relation.HasMany<U, V> relation, Query.Filter<V> filter) {
    // append the relational filter criteria by default
    ModelStructure<ModelIntermediate> intermediate = relation.getIntermediateTable();

    //
    Column<V, Long> pCol = (Column<V, Long>)relation.getTargetType().getColumn(relation.getTargetFieldName());
    filter.and(pCol.eq(new Query.Parameter<Long>(this.getId())));
    Query.Builder<V> builder = relation.getTargetType().query();
    if (intermediate == null) {
      // Retrieve the data based on the filter
      builder.where(filter).and(pCol.eq(this.getId()));
    } else {

      Column.BackReference<ModelIntermediate,?> backRef
              = (Column.BackReference)intermediate.getColumn(relation.getTargetFieldName());
      Column f = intermediate.getColumn(relation.getSourceFieldName());

      Query.Join<ModelIntermediate> intermediateJoin = new Query.Join<>(backRef);
      intermediateJoin.filter().and(f.eq(this.getId()));
      builder.join(intermediateJoin);

    }

    RelationalData<V> d = (RelationalData<V>)hasManyData.get(relation);
    d.clear();

    List<V> res = builder.build().execute().toList();
    res.forEach(d::add);

    return d.getAll();
  }

  @Override
  public <U extends Model, V extends Model> V add(Relation.HasMany<U, V> relation, V model) {
    checkLock();

    // Just need to add this on the list
    RelationalData<V> d = (RelationalData<V>)hasManyData.get(relation);
    if (d == null) {
      d = new RelationalData<>();
      hasManyData.put(relation, d);
    }
    d.newRecords.add(model);
    return model;
  }

  @Override
  public <U extends Model, V extends Model> void remove(Relation.HasMany<U, V> relation, V model) {
    checkLock();

    RelationalData<V> d = (RelationalData<V>)hasManyData.get(relation);
    if (d != null) {
      d.newRecords.remove(model);
    }
  }

  /* A transient variable used to avoid circular reference */
  private transient boolean saving = false;

  @Override
  public boolean save(DBSession session) {
    // Hopefully we haven't reached here via circular reference in a relationship
    if (saving) {
      return false;
    }

    // Only go through the hardship if any modification has been done
    if (!isModified()) {
      return false;
    }

    // Set flag to mark the beginning of the process
    saving = true;

    // Stage 1 - Save all the referenced record
    // First we save all the referenced data
    for(Map.Entry<Column.Reference, Model> entry:referencedData.entrySet()) {
      Model m = entry.getValue();
      // Save the record
      m.save(session);

      // update the model with the referenced id
      set(entry.getKey(), m.getId());
    }

    // Flag to see if the primary model is actual an insert or a update
    boolean isNew = this.getId() == null;

    // Stage 2 - Save all the implementations
    ModelStructure structure = this.getStructure();
    int implStart = structure.getParents().length + 1;
    int i=implStart;
    Collection<ModelStructure> implementations = structure.getImplementations();
    for(ModelStructure impl:implementations) {
      ModelRow row = set.allRows[i++];
      if (row == null) {      // Either we don't need to do anything or insert a new row
        if (isNew) {          // this is an insert
          row = new ModelRow(impl);
          session.insert(row);
        } else {
          continue;
        }
      } else {
        if (row.isModified()) {
          if(isNew) {
            session.insert(row);
          } else {
            session.update(row, getId());
          }
        } else {
          continue;
        }
      }

      // Update the implementation id on the respective row
      set((Column<?, Long>) structure.getImplementationColumn(impl), row.getId());
    }

    // Stage 3. Save the model hierarchy
    for(i=0; i<implStart; ++i) {
      ModelRow row = set.allRows[i];
      ModelStructure st;
      if (row == null) {
        if (isNew) {
          st = structure.getParents()[i];   // Recovering the structure here through
                                            // when 'i' can actually go above the
                                            // limit as 'this' model itself is not
                                            // in the parent list, but we rely on
                                            // the fact that a primaryRow is always
                                            // defined so 'row == null' will be false
          row = new ModelRow(st);
          row.set(0, getId());
          session.insert(row);
          set.primaryRow.set(0, row.getId());
        }
      } else {
        if (row.isModified()) {
          row.set(0, getId());
          st = row.getStructure();
          if(isNew) {
            session.insert(row);
            set.primaryRow.set(0, row.getId());
          } else {
            session.update(row, getId());
          }
        }
      }
    }

    // Stage 4. Save the has many relation data
    Set<Map.Entry<Relation.HasMany, RelationalData>> entries = hasManyData.entrySet();
    for(Map.Entry<Relation.HasMany, RelationalData> entry:entries) {
      Relation.HasMany ref = entry.getKey();
      RelationalData d = entry.getValue();

      // Check if there is an intermediate table involved. In case there is
      // an intermediate table, the referenced model should be saved first
      // and then intermediate table records need to be saved after that
      ModelStructure intermediate = ref.getIntermediateTable();
      List<Model> all = d.getAll();
      if (intermediate == null) {
        for (Model m : all) {
          // update the parent model primary id used for referencing
          m.set(ref.getBackReference(), getId());

          // Save the referenced model (recursively)
          m.save(session);
        }
      } else {
        for (Model m : all) {
          // Save the referenced model
          m.save(session);

          // Now save the intermediate table
          session.updateIntermediate(intermediate, ref, this, m);
        }
      }
    }

    saving = false;
    return true;
  }

  @Override
  public JSONObject toJSON(int level) {
    // In case of nested JSON creation, we will nest till 5th level
    // only, otherwise we may fall in the circular reference trap
    if (level == 7) {
      return null;
    }
    JSONObject o = new JSONObject();

    ModelStructure m = this.getStructure();

    // first set the type
    ModelStructure[] parents = m.getParents();
    if (parents.length == 0) {
      o.put("_type", m.getTableName());
    } else {
      JSONArray types = new JSONArray();
      types.put(0, m.getTableName());
      for(int i=1; i<=parents.length; ++i) {
        types.put(i, parents[i-1].getTableName());
      }
      o.put("_type", types);
    }

    // next get all the values and put them up
    List<Column> cols = m.getColumns();
    for(Column c:cols) {
      updateJSON(o, c.getFieldName(), this.get(c));
    }
    for(int i=0; i<parents.length; ++i) {
      cols = parents[i].getColumns();
      for(Column c:cols) {
        updateJSON(o, c.getFieldName(), this.get(c));
      }
    }

    // now the references
    // first is the BelongsTo reference
    for(Map.Entry<Column.Reference, Model> entry:this.referencedData.entrySet()) {
      updateJSON(o, entry.getKey().getRelationName(),
              entry.getValue().toJSON(level+1));
    }
    // Next is the has many relation
    for(Map.Entry<Relation.HasMany, RelationalData> entry: this.hasManyData.entrySet()) {
      JSONArray ar = new JSONArray();

      List<Model> list = entry.getValue().getAll();
      for(Model data:list) {
        ar.put(data.toJSON(level + 1));
      }

      updateJSON(o, entry.getKey().getRelationName(), ar);
    }


    return o;
  }

  private void updateJSON(JSONObject obj, String key, Object value) {
    if(value instanceof Date) {
      obj.put(key, Schema.ISO_8601_DATE_TIME.format((Date) value));
    } else {
      obj.put(key, value);
    }
  }
}
