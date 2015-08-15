package net.symplifier.db;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * The basic model components that identifies a column in a table
 *
 * @param <M> The type of the Model
 * @param <T> The type of the field
 */
public class Column<M extends Model, T> implements ModelComponent<M> {

  private final Class<T> valueType;

  private final Cache<T, ModelRow<M>> cache;

  /* The model to which this column belongs */
  private ModelStructure<M> model;
  /* The position of the model on the hierarchy */
  private int level;
  /* The variable name of this column in the model */
  private String name;
  /* The field name of this column in the database */
  private String fieldName;

  /* The position of this column in the model */
  private int index;

  public Column(Class<T> valueType) {
    this(valueType, new Builder());
  }

  public Column(Class<T> valueType, Builder builder) {
    this.valueType = valueType;
    this.fieldName = builder.getName();

    cache = builder.cacheLimit <= 0 ? null :
            CacheBuilder.newBuilder().maximumSize(builder.getCacheLimit()).build();

  }

  @Override
  public void onInit(ModelStructure<M> structure) {
    this.model = structure;
    this.index = structure.getColumnCount();
    this.level = structure.getParents().length;
  }

  /**
   * Get the value type
   * @return {@link Class} value type
   */
  public Class<T> getValueType() {
    return valueType;
  }

  /**
   * Retrieve the position of the column within the model
   *
   * @return 0 based position of the column in the model
   */
  public int getIndex() {
    return index;
  }

  /**
   * Retrieve the position of the model within the hierarchy
   *
   * @return 0 based position of the model in the hierarcy
   */
  public int getLevel() {
    return level;
  }

  /**
   * Retrieve the name of the field in the database for this column
   *
   * @return field name
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Retrieve the owner model of this column
   * @return {@link ModelStructure}
   */
  public ModelStructure<M> getModel() {
    return model;
  }

  /**
   * A reference column that points to another model
   *
   * @param <M> The Source Model
   * @param <T> The Target Model
   */
  public static class Reference<M extends Model, T extends Model>
          extends Column<M, Long> implements net.symplifier.db.Reference<M, T> {

    private final Class<T> referenceType;
    private ModelStructure<T> referenceModel;

    public Reference(Class<T> referenceType) {
      this(referenceType, new Builder());
    }

    public Reference(Class<T> referenceType, Builder builder) {
      super(Long.class, builder);
      this.referenceType = referenceType;
    }

    public void onInit(ModelStructure<M> structure) {
      super.onInit(structure);
      referenceModel = structure.getSchema().registerModel(referenceType);
    }

    @Override
    public ModelStructure<M> getSourceType() {
      return getModel();
    }

    @Override
    public ModelStructure<T> getTargetType() {
      return referenceModel;
    }

    @Override
    public String getSourceFieldName() {
      return getFieldName();
    }

    @Override
    public String getTargetFieldName() {
      return referenceModel.getPrimaryKeyField();
    }
  }

  /**
   * A column used to link to a interface model
   *
   * @param <M> The source model
   * @param <T> The target model
   */
  public static class Interface<M extends Model, T extends ModelInterface>
          extends Reference<M, T> {

    public Interface(Class<T> interfaceType) {
      this(interfaceType, new Builder());
    }

    public Interface(Class<T> interfaceType, Builder builder) {
      super(interfaceType, builder);
    }
  }

  /**
   * Column builder
   */
  public static class Builder {
    private String name = null;
    private int cacheLimit = 0;

    /**
     * Set the name to be used for setting the column field name
     * @param name The name to be used as column field name
     * @return self chaining
     */
    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    /**
     * Set cache size for the column
     * @param limit The number of records to store in cache
     * @return self chaining
     */
    public Builder setCacheLimit(int limit) {
      this.cacheLimit = limit;
      return this;
    }


    /**
     * The field name for the column
     * @return string
     */
    String getName() {
      return name;
    }

    /**
     * The number of records to be stored in cache
     * @return 0 if cache is disabled otherwise cache size
     */
    int getCacheLimit() {
      return cacheLimit;
    }
  }

  public static class String<M extends Model> extends Column<M, java.lang.String> {

    public String() {
      super(java.lang.String.class);
    }

    public String(Builder builder) {
      super(java.lang.String.class, builder);
    }
  }

  public static class Integer<M extends Model> extends Column<M, java.lang.Integer> {
    public Integer() {
      super(java.lang.Integer.class);
    }

    public Integer(Builder builder) {
      super(java.lang.Integer.class, builder);
    }
  }

  public static class Date<M extends Model> extends Column<M, java.util.Date> {
    public Date() {
      super(java.util.Date.class);
    }

    public Date(Builder builder) {
      super(java.util.Date.class, builder);
    }
  }
}