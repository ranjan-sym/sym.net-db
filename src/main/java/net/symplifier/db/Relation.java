package net.symplifier.db;

/**
 * The Relation type interface
 *
 * Created by ranjan on 8/14/15.
 */
public interface Relation {

  class BelongsTo<M extends Model, T extends Model> extends Column.Reference<M, T> {

    public BelongsTo(Class<T> referenceType) {
      super(referenceType);
    }

    public BelongsTo(Class<T> referenceType, Builder builder) {
      super(referenceType, builder);
    }

  }

  class HasOne<M extends Model, T extends Model> implements Reference<M, T> {
    private ModelStructure<M> sourceModel;
    private ModelStructure<T> targetModel;

    private final Class<T> targetType;
    private String targetFieldName;

    public HasOne(Class<T> targetType) {
      this(targetType, null);
    }
    public HasOne(Class<T> targetType, String targetFieldName) {
      this.targetType = targetType;
      this.targetFieldName = targetFieldName;
    }

    @Override
    public void onInit(ModelStructure<M> owner) {
      this.sourceModel = owner;

      targetModel = owner.getSchema().registerModel(targetType);
      if (targetFieldName == null) {
        targetFieldName = owner.getTableName() + "_id";
      }
    }

    @Override
    public ModelStructure<M> getSourceType() {
      return sourceModel;
    }

    @Override
    public ModelStructure<T> getTargetType() {
      return targetModel;
    }

    @Override
    public String getSourceFieldName() {
      return sourceModel.getPrimaryKeyField();
    }

    @Override
    public String getTargetFieldName() {
      return targetFieldName;
    }


  }

  class HasMany<M extends Model, T extends Model> implements Reference<M, T> {
    private ModelStructure<M> sourceModel;
    private ModelStructure<T> targetModel;

    private final Class<T> targetType;
    private final ModelStructure<? extends Model> intermediateTable;
    private String sourceFieldName;
    private String targetFieldName;

    public HasMany(Class<T> targetModel) {
      this(targetModel, null, null, null);
    }

    public HasMany(Class<T> targetModel, Column.Reference<T, M> reference) {
      this(targetModel, reference.getTargetFieldName(), null, null);
    }

    public HasMany(Class<T> targetModel, String targetFieldName, ModelStructure<? extends Model> intermediateTable, String sourceFieldName) {
      this.targetType  = targetModel;
      this.targetFieldName = targetFieldName;
      this.intermediateTable = intermediateTable;
      this.sourceFieldName = sourceFieldName == null ? sourceModel.getPrimaryKeyField() : sourceFieldName;
    }

    public void onInit(ModelStructure<M> model) {
      this.sourceModel = model;
      this.targetModel = model.getSchema().registerModel(targetType);
      if (targetFieldName == null) {
        targetFieldName = model.getTableName() + "_id";
      }
    }

    @Override
    public ModelStructure<M> getSourceType() {
      return sourceModel;
    }

    @Override
    public ModelStructure<T> getTargetType() {
      return targetModel;
    }

    @Override
    public ModelStructure<? extends Model> getIntermediateTable() {
      return intermediateTable;
    }

    public String getSourceFieldName() {
      return sourceFieldName;
    }

    public String getTargetFieldName() {
      return targetFieldName;
    }


  }
}