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

//  class HasOne<M extends Model, T extends Model> implements Reference<M, T> {
//    private ModelStructure<M> sourceModel;
//    private ModelStructure<T> targetModel;
//
//    private final Class<T> targetType;
//    private String targetFieldName;
//
//    public HasOne(Class<T> targetType) {
//      this(targetType, null);
//    }
//    public HasOne(Class<T> targetType, String targetFieldName) {
//      this.targetType = targetType;
//      this.targetFieldName = targetFieldName;
//    }
//
//    public void onInit(ModelStructure<M> owner) {
//      this.sourceModel = owner;
//
//      targetModel = owner.getSchema().registerModel(targetType);
//      if (targetFieldName == null) {
//        targetFieldName = owner.getTableName() + "_id";
//      }
//    }
//
//    @Override
//    public ModelStructure<M> getSourceType() {
//      return sourceModel;
//    }
//
//    @Override
//    public ModelStructure<T> getTargetType() {
//      return targetModel;
//    }
//
//    @Override
//    public String getSourceFieldName() {
//      return sourceModel.getPrimaryKeyField();
//    }
//
//    @Override
//    public String getTargetFieldName() {
//      return targetFieldName;
//    }
//
//
//  }

  class HasMany<M extends Model, T extends Model> implements Reference<M, T> {
    private ModelStructure<M> sourceModel;
    private ModelStructure<T> targetModel;

    private final Class<T> targetType;
    private final String intermediateTableName;

    /*
       The intermediate table is a special table which will not have a corresponding
       model in the ORM. The intermediate table consists of only two columns both
       as a composite primary key. If the relationship consists of some fields,
       then it won't be a many-to-many relationship any more, but just a one to
       many and many to one relationship to another model in between
     */
    private ModelStructure<ModelIntermediate> intermediateTable;
    private String sourceFieldName;
    private String targetFieldName;

    public HasMany(Class<T> targetModel) {
      this(targetModel, null, null, null);
    }

    public HasMany(Class<T> targetModel, Column.Reference<T, M> reference) {
      this(targetModel, reference.getTargetFieldName(), null, null);
    }

    public HasMany(Class<T> targetModel, String intermediateTable) {
      this(targetModel, null, intermediateTable, null);
    }

    public HasMany(Class<T> targetModel, String targetFieldName, String intermediateTable, String sourceFieldName) {
      this.targetType  = targetModel;
      this.targetFieldName = targetFieldName;
      this.intermediateTableName = intermediateTable;
      this.sourceFieldName = sourceFieldName;
    }

    public void onInit(ModelStructure<M> model) {
      this.sourceModel = model;
      this.targetModel = model.getSchema().registerModel(targetType);

      if (sourceFieldName == null) {
        this.sourceFieldName = model.getPrimaryKeyField();
      }
      if (targetFieldName == null) {
        targetFieldName = model.getTableName() + "_id";
      }

      if (intermediateTableName != null) {
        intermediateTable = model.getSchema()
                .registerIntermediateModel(intermediateTableName,
                        this.sourceModel.getType(),
                        this.targetModel.getType());
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
    public ModelStructure<ModelIntermediate> getIntermediateTable() {
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