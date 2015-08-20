package net.symplifier.db;

import java.util.*;

/**
 * Created by ranjan on 7/28/15.
 */
public class ModelInstance<M extends ModelInstance> implements Model {

  private final ModelStructure<? extends M> structure;


  public ModelInstance(ModelStructure<? extends M> structure) {
    set = new ModelSet();
    this.structure = structure;
  }

  @Override
  public ModelStructure getStructure() {
    return structure;
  }

  public Long getId() {
    return set.rows[0].getId();
  }

  @Override
  public <T> T get(Column<?, T> column, int level) {
    return (T)set.rows[level].get(column.getIndex());
  }

  @Override
  public <T> void set(Column<?, T> column, int level, T value) {
    ModelRow row = set.rows[level];

    int colIndex = column.getIndex();
    Object chk = row.get(colIndex);

    // Let's first see if its actually a change
    if (value == chk || (value != null && value.equals(chk))) {
      return;
    }

    // If we are modifying a new row, we will createModel a copy
    // and work on that
    if (!row.isModified()) {
      row = set.rows[level] = row.copy();
    }

    // finally change the value and mark it dirty
    row.set(colIndex, value, true);
  }

  public boolean isModified() {
    for(ModelRow r:set.rows) {
      if(r.isModified()) {
        return true;
      }
    }
    return false;
  }

  public class ModelSet {
    private final ModelRow[] rows;
    public ModelSet() {
      rows = new ModelRow[structure.getEffectiveTablesCount()];
      for(int i=0;i<rows.length; ++i) {
        rows[i] = new ModelRow(structure);
      }
    }
  }

  private final ModelSet set;

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

    public List<V> getAll() {
      List<V> res = new ArrayList<>(existingRecords.size() + newRecords.size());

      res.addAll(existingRecords.values());
      res.addAll(newRecords);

      return res;
    }

    public void add(V record) {
      Long id = record.getId();
      if (id == null) {
        newRecords.add(record);
      } else {
        existingRecords.put(id, record);
      }
    }
  }

  private final Map<Reference, RelationalData<? extends Model>> relationalData = new HashMap<>();

  /**
   * Clears all relational data
   */
  public void clearAll() {
    relationalData.clear();
  }

  /**
   * Clears the data for specific relation only
   *
   * @param relation The relation which needs to be cleared
   * @param <U> The relationship owner model type
   * @param <V> The relationship target model type
   */
  public <U extends M, V extends Model> void clear(Relation.HasMany<U, V> relation) {
    relationalData.remove(relation);
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
    if (relationalData.containsKey(relation)) {
      return (List<V>)relationalData.get(relation).getAll();

    } else {
      return loadAll(relation);
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

    RelationalData<V> d = (RelationalData<V>)relationalData.get(relation);
    d.clear();

    List<V> res = builder.build().execute().toList();
    res.forEach(d::add);

    return d.getAll();
  }

  public <U extends M, V extends Model> V add(Relation.HasMany<U, V> relation, V model) {
    // Just need to add this on the list
    RelationalData<V> d = (RelationalData<V>)relationalData.get(relation);
    if (d == null) {
      d = new RelationalData<>();
    }
    d.newRecords.add(model);
    return model;
  }

  public void save() {
    // Get all the different rows and save them all separately
    for(int i=0; i<set.rows.length;++i) {
      ModelRow row = set.rows[i];

      // Save only those rows that have been modified
      if (row.isModified()) {


      }

    }

  }



}
