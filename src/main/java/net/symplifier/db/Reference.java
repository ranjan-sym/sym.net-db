package net.symplifier.db;

/**
 *
 * The Reference provides a mechanism for joining one model to another. The models
 * can have "reference columns" that link to some other model or different types
 * of "relation" that links to other models.
 *
 * Created by ranjan on 7/30/15.
 *
 * @param <M> The source model
 * @param <T> The target model
 */
public interface Reference<M extends Model, T extends Model> {

  /**
   * Retrieve the name of the relationship as defined in the Java Model class
   *
   * @return The name of the relation (camelCase)
   */
  String getRelationName();

  /**
   * Sets the name of the relationship
   *
   * @param name The name of the relationship (camelCase);
   */
  void setRelationName(String name);

  /**
   * Retrieve the information of the Source Model
   *
   * @return The {@link ModelStructure} of the source model
   */
  ModelStructure<M> getSourceType();

  /**
   * Retrieve the information of the Target model
   *
   * @return The {@link ModelStructure} of the target model
   */
  ModelStructure<T> getTargetType();

  /**
   * Makes the join between the models automatic, i.e., even if the user doesn't
   * use this reference in the join explicitly, this reference would be used
   * anyway.
   *
   * Note: Not implemented yet. Should always return {@code false}
   *
   * @return {@code false} as this feature is not implemented yet
   */
  default boolean isAutoLoaded() {
    return false;
  }
  /**
   * Makes the join between the models either inner or outer (LEFT)
   *
   * @return {@code true} if the relationship is inclusive otherwise {@code false}
   */
  default boolean isInner() {
    return false;
  }

  /**
   * Get intermediate model in case of many to many joins that facilitates the
   * join
   *
   * @return The structure of intermediate {@link ModelStructure} model
   */
  default ModelStructure<ModelIntermediate> getIntermediateTable() {
    return null;
  }

  /**
   * Retrieve the name of the field in the source model used for joining the
   * models.
   *
   * @return The field name on the source table
   */
  String getSourceFieldName();

  /**
   * Retrieve the name of the field in the target model used for joining the
   * models
   *
   * @return The field name on the source table
   */
  String getTargetFieldName();

  /**
   * Provide filter conditions on the referenced model for limiting the result.
   *
   * @param filter The filter condition
   * @return Returns a {@link net.symplifier.db.Query.Join} for chaining joins
   */
  default Query.Join<T> where(Query.Filter<T> filter) {
    return new Query.Join<>(this, filter);
  }

  /**
   * Provide ascending order by columns for ordering the result set
   *
   * @param columns List of column that are used for ordering
   * @return Returns a {@link net.symplifier.db.Query.Join} for chaining joins
   * @see {@link #desc(Column[])}
   */
  default Query.Join<T> asc(Column<T, ?> ... columns) {
    Query.Join<T> j = new Query.Join<>(this);
    j.asc(columns);
    return j;
  }

  /**
   * Provide descending order by columns for ordering the result set
   *
   * @param columns List of column that are used for ordering
   * @return Returns a {@link net.symplifier.db.Query.Join} for chaining joins
   * @see {@link #asc(Column[])}
   */
  default Query.Join<T> desc(Column<T, ?> ... columns) {
    Query.Join<T> j = new Query.Join<>(this);
    j.desc(columns);
    return j;
  }

  default <U extends T> Query.Join<U> as(Class<U> parentClass) {
    return new Query.Join<U>(getSourceType().getSchema().getModelStructure(parentClass), this);
  }

  /**
   * Join another model through a referenced model
   *
   * @param reference The reference to another model in the referenced model
   * @param <U> The type of the another model being joined
   * @return A {@link net.symplifier.db.Query.Join} object for chaining
   */
  default <U extends Model> Query.Join<T> join(Reference<? super T, U> reference) {
    Query.Join<T> j = new Query.Join<>(this);
    return j.join(reference);
  }

  /**
   * Join another model with filters through a referenced model. A helper
   * mechanism to join after a filter has been applied on the source reference
   * during initial join
   *
   * @param join The join object that had been returned by {@link Reference#where(Query.Filter)}
   *             during parent level join
   * @param <U> The type of the another model being joined
   * @return A {@link net.symplifier.db.Query.Join} object for chaining
   */
  default <U extends Model> Query.Join<T> join(Query.Join<U> join) {
    Query.Join<T> j = new Query.Join<>(this);
    return j.join(join);
  }

  void onInitReference(ModelStructure<M> owner);

}
