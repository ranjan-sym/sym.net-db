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
            CacheBuilder.newBuilder().maximumSize(builder.cacheLimit).build();

  }

  @Override
  public void onInit(ModelStructure<M> structure) {
    this.model = structure;
    this.index = structure.getColumnCount();
    this.level = structure.getParents().length;
  }

  public int getIndex() {
    return index;
  }

  public int getLevel() {
    return level;
  }

  public String getFieldName() {
    return fieldName;
  }

  public ModelStructure<M> getModel() {
    return model;
  }

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

  public static class Interface<M extends Model, T extends ModelInterface>
          extends Column<M, Long> implements net.symplifier.db.Reference<M, T> {

    private final Class<T> interfaceType;
    private ModelStructure<T> interfaceModel;

    public Interface(Class<T> interfaceType) {
      this(interfaceType, new Builder());
    }

    public Interface(Class<T> interfaceType, Builder builder) {
      super(Long.class, builder);

      this.interfaceType = interfaceType;
    }

    @Override
    public void onInit(ModelStructure<M> structure) {
      super.onInit(structure);

      this.interfaceModel = structure.getSchema().registerModel(interfaceType);
    }


    @Override
    public ModelStructure<M> getSourceType() {
      return getModel();
    }

    @Override
    public ModelStructure<T> getTargetType() {
      return interfaceModel;
    }

    @Override
    public String getSourceFieldName() {
      return getFieldName();
    }

    @Override
    public String getTargetFieldName() {
      return interfaceModel.getPrimaryKeyField();
    }
  }

  public static class Builder {
    private String name = null;
    private int cacheLimit = 0;

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setCacheLimit(int limit) {
      this.cacheLimit = limit;
      return this;
    }

    String getName() {
      return name;
    }

    int getCacheLimit() {
      return cacheLimit;
    }
  }
}