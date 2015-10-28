package net.symplifier.db;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.symplifier.db.exceptions.ModelException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The basic model components that identifies a column in a table
 *
 * @param <M> The type of the Model
 * @param <T> The type of the field
 */
public abstract class Column<M extends Model, T> implements Query.FilterEntity {

  private final Class<T> valueType;

  private final Cache<T, ModelRow> cache;

  private final T defaultValue;

  private final boolean canBeNull;

  /* The model to which this column belongs */
  private ModelStructure<M> model;
  /* The position of the model on the hierarchy */
  private int level;
  /* The variable name of this column in the model */
  private java.lang.String name;
  /* The field name of this column in the database */
  private java.lang.String fieldName;
  /*  */
  private java.lang.String caption;

  private Integer minimumLength;
  private Integer maximumLength;
  private T lowThreshold;
  private T highThreshold;
  private boolean required;
  private boolean unique;
  private List<Validator> validators;
  private Map<String, String> properties;

  private Object parameterSetter;

  private Object field;

  final Map<ModelStructure, java.lang.Integer> implementationLevel = new HashMap<>();

  /* The position of this column in the model */
  private int index;

  public Column(Class<T> valueType) {
    this(valueType, new Builder<>());
  }

  public Column(Class<T> valueType, Builder<T> builder) {
    this.valueType = valueType;
    this.fieldName = builder.getName();
    this.defaultValue = builder.getDefaultValue();
    this.canBeNull = builder.canBeNull();
    this.caption = builder.caption;
    this.minimumLength = builder.minimumLength;
    this.maximumLength = builder.maximumLength;
    this.lowThreshold  = builder.lowThreshold;
    this.highThreshold = builder.highThreshold;
    this.required = builder.required;
    this.unique = builder.unique;
    this.validators = builder.validators;
    this.properties= builder.properties;

    cache = builder.cacheLimit <= 0 ? null :
            CacheBuilder.newBuilder().maximumSize(builder.getCacheLimit()).build();



  }

  public JSONObject getMetaData() {
    JSONObject o = new JSONObject();
    o.put("name", this.fieldName);
    o.put("caption", this.caption);
    o.put("type", this.getTypeText());
    if (this.required) {
      o.put("required", true);
    }
    if (this.unique) {
      o.put("unique", true);
    }
    if (this.defaultValue != null) {
      o.put("default", this.defaultValue);
    }
    // Go through all the properties
    if (this.properties != null) {
      for(Map.Entry<String, String> entry: this.properties.entrySet()) {
        o.put(entry.getKey(), entry.getValue());
      }
    }

    // Add all the validators
    if (this.validators != null) {
      JSONArray validators = new JSONArray();
      for(Validator v:this.validators) {
        validators.put(v.getJSON());
      }
      o.put("validators", validators);
    }



    return o;
  }


  public String getThreshold() {
    String res = "";
    if (lowThreshold != null) {
      res += lowThreshold;
    }
    if (highThreshold != null) {
      if (!res.isEmpty()) {
        res += ",";
      }
      res += highThreshold;
    }
    if (!res.isEmpty()) {
      res = "(" + res + ")";
    }
    return res;
  }

  public String getLengthLimit() {
    String res = "";
    if (minimumLength != null) {
      res += minimumLength;
    }
    if (maximumLength != null) {
      if (!res.isEmpty()) {
        res += ",";
      }
      res += maximumLength;
    }
    if (!res.isEmpty()) {
      res = "(" + res + ")";
    }
    return res;
  }

  /**
   * Retrieve the default value set for this column
   *
   * @return The default value set in the model
   */
  public T getDefaultValue() {
    return defaultValue;
  }

  public abstract String getTypeText();


  public String getCaption() {
    return caption;
  }

  /**
   * Method used for generating default field name based on their name, this
   * method is available for overriding since we could have different types of
   * column that may have a different mechanism of generating default field
   * name.
   *
   * <p>
   *   Note however that this method is invoked only if a manual field name
   *   has not been provided
   * </p>
   *
   * @return The default field name to be used for this column
   */
  protected String generateFieldName() {
    return Schema.toDBName(name);
  }


  public void onInit(ModelStructure<M> structure) {
    this.model = structure;
    this.index = structure.getColumnCount();
    this.level = structure.getParents().length;
    this.parameterSetter = structure.getSchema().getDriver().getParameterSetter(this.valueType);
    this.field = structure.getSchema().getDriver().getField(this.valueType);

    if (fieldName == null) {
      fieldName = generateFieldName();
    }
  }

  public boolean isPrimary() {
    return false;
  }



  public Object getParameterSetter() {
    return parameterSetter;
  }

  public Object getField() {
    return field;
  }

  String getName() {
    return this.name;
  }

  void setName(java.lang.String name) {
    this.name = name;
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

  public int getLevel(ModelStructure structure) {
    java.lang.Integer l = implementationLevel.get(structure);
    if (l == null) {
      throw new ModelException(getModel().getType(), "Invalid model structure. Cannot find implementation of " + structure.getTableName());
    } else {
      return l;
    }
  }

  /**
   * Retrieve the name of the field in the database for this column
   *
   * @return field name
   */
  public java.lang.String getFieldName() {
    return fieldName;
  }

  /**
   * Retrieve the owner model of this column
   * @return {@link ModelStructure}
   */
  public ModelStructure<M> getModel() {
    return model;
  }


  /* BEGIN - The query helper methods */

  public Query.Filter<M> eq(T value) {
    return eq(new Query.Parameter<>(value));
  }

  public Query.Filter<M> like(T value) {
    return like(new Query.Parameter<>(value));
  }

  @SafeVarargs
  public final Query.Filter<M> in(T ... values) {
    return op(Query.FilterOp.in, new Query.ParameterList<>(values).init(this));
  }

  public Query.Filter<M> notEq(T value) {
    return notEq(new Query.Parameter<>(value));
  }

  public Query.Filter<M> lt(T value) {
    return lt(new Query.Parameter<>(value));
  }

  public Query.Filter<M> gt(T value) {
    return gt(new Query.Parameter<>(value));
  }

  public Query.Filter<M> ltEq(T value) {
    return ltEq(new Query.Parameter<>(value));
  }

  public Query.Filter<M> gtEq(T value) {
    return gtEq(new Query.Parameter<>(value));
  }

  private Query.Filter<M> op(Query.FilterOp op) {
    Query.Filter<M> f = new Query.Filter<>();
    f.append(this);
    f.append(op);
    return f;
  }

  private Query.Filter<M> op(Query.FilterOp op, Query.FilterEntity value) {
    Query.Filter<M> f = new Query.Filter<>();
    f.append(this);
    f.append(op);
    f.append(value);
    return f;
  }

  public Query.Filter<M> eq(Query.Parameter<T> value) {
    return op(Query.FilterOp.eq, value.init(this));
  }

  public Query.Filter<M> notEq(Query.Parameter<T> value) {
    return op(Query.FilterOp.notEq, value.init(this));
  }

  @SafeVarargs
  public final Query.Filter<M> in(Query.Parameter<T> ... values) {
    return op(Query.FilterOp.in, new Query.ParameterList<>(values).init(this));
  }

  public Query.Filter<M> like(Query.Parameter<T> value) {
    return op(Query.FilterOp.like, value.init(this));
  }


  public Query.Filter<M> lt(Query.Parameter<T> value) {
    return op(Query.FilterOp.lt, value.init(this));
  }

  public Query.Filter<M> gt(Query.Parameter<T> value) {
    return op(Query.FilterOp.gt, value.init(this));
  }

  public Query.Filter<M> ltEq(Query.Parameter<T> value) {
    return op(Query.FilterOp.ltEq, value.init(this));
  }

  public Query.Filter<M> gtEq(Query.Parameter<T> value) {
    return op(Query.FilterOp.gtEq, value.init(this));
  }

  public Query.Filter<M> isNull() {
    return op(Query.FilterOp.isNull);
  }

  public Query.Filter<M> isNotNull() {
    return op(Query.FilterOp.isNotNull);
  }

  public boolean canBeNull() {
    return canBeNull;
  }


  public static class Primary<M extends Model> extends Column<M, Long> {

    public Primary() {
      super(Long.class);
    }

    public Primary(Builder<Long> builder) {
      super(Long.class, builder);
    }

    @Override
    public boolean isPrimary() {
      return true;
    }

    public String getTypeText() {
      return "INT";
    }
  }


  /**
   * A reference column that points to another model
   *
   * @param <M> The Source Model
   * @param <T> The Target Model
   */
  public static class Reference<M extends Model, T extends Model>
          extends Column<M, java.lang.Long> implements net.symplifier.db.Reference<M, T> {

    private final Class<T> referenceType;
    private ModelStructure<T> referenceModel;

    public Reference(Class<T> referenceType) {
      this(referenceType, new Builder<>());
    }

    public Reference(Class<T> referenceType, Builder<java.lang.Long> builder) {
      super(java.lang.Long.class, builder);
      this.referenceType = referenceType;
    }

    public String getTypeText() {
      return "REF(" + referenceModel.getTableName() + ")";
    }

    @Override
    public void onInitReference(ModelStructure<M> owner) {

    }

    @Override
    public void onInit(ModelStructure<M> structure) {
      super.onInit(structure);
      referenceModel = structure.getSchema().registerModel(referenceType);
    }

    public String getRelationName() {
      return getName();
    }

    public void setRelationName(String name) {
      assert(name == this.getName());
      // no need to do anything
    }

    @Override
    protected String generateFieldName() {
      return super.generateFieldName() + "_id";
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
    public java.lang.String getSourceFieldName() {
      return getFieldName();
    }

    @Override
    public java.lang.String getTargetFieldName() {
      return referenceModel.getPrimaryKeyField();
    }
  }

  /**
   * A very special type of reference used for back-referencing one to many
   * relationship in the many to many relationship. This one is used in the
   * intermediate table join condition during the data retrieval in HasMany
   * relationship (used in ModelInstance only at the moment)
   * @param <T> The Intermediate model
   * @param <M> The source model
   */
  public static class BackReference<T extends Model, M extends Model>
          extends Column<T, Long> implements net.symplifier.db.Reference<M, T> {

    private final Class<M> sourceType;
    private ModelStructure<T> targetModel;
    private ModelStructure<M> sourceModel;
    public BackReference(Class<M> sourceType) {
      super(Long.class);
      this.sourceType = sourceType;
    }

    public String getTypeText() {
      return "INT";
    }

    @Override
    public void onInitReference(ModelStructure<M> owner) {

    }

    @Override
    public void setRelationName(String name) {
      assert(name == getName());
    }

    @Override
    public String getRelationName() {
      return getName();
    }

    @Override
    protected String generateFieldName() {
      return super.generateFieldName() + "_id";
    }

    public void onInit(ModelStructure<T> structure) {
      super.onInit(structure);

      this.targetModel = structure;
      this.sourceModel = structure.getSchema().getModelStructure(sourceType);
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
      return getFieldName();
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
      this(interfaceType, new Builder<>());
    }

    public Interface(Class<T> interfaceType, Builder<java.lang.Long> builder) {
      super(interfaceType, builder);
    }
  }

  public static abstract class CustomType<T> {
    protected T genericValue;

    public CustomType(T generic) {
      this.genericValue = generic;
    }

    public T getGeneric() {
      return genericValue;
    }

    public void setGeneric(T value) {
      this.genericValue = value;
    }
  }

  public static abstract class Custom<M extends Model, G, T extends CustomType> extends Column<M, G> {

    private final Class<T> customType;

    public Custom(Class<T> customType, Class<G> genericType) {
      this(customType, genericType, new Builder<>());
    }

    public Custom(Class<T> customType, Class<G> genericType, Builder<G> builder) {
      super(genericType, builder);
      this.customType = customType;
    }

    public Class<T> getCustomType() {
      return customType;
    }

    public abstract T getFromGeneric(G value);

    public abstract G getGeneric(T value);
  }

  public static class Validator {
    private String check;
    private String message;

    public Validator(String check, String message) {
      this.check = check;
      this.message = message;
    }

    public JSONObject getJSON() {
      JSONObject o = new JSONObject();
      o.put("check", check);
      o.put("message", message);
      return o;
    }
  }
  /**
   * Column builder
   */
  public static class Builder<T> {
    private java.lang.String name = null;
    private java.lang.String caption = null;
    private boolean required = false;
    private boolean unique = false;
    private int cacheLimit = 0;
    private boolean allowNull = false;
    private T defaultValue = null;
    private T lowThreshold = null;
    private T highThreshold = null;
    private Integer minimumLength = null;
    private Integer maximumLength = null;
    private List<Validator> validators = null;
    private Map<String, String> properties = null;
    /**
     * Set the name to be used for setting the column field name
     * @param name The name to be used as column field name
     * @return self chaining
     */
    public Builder<T> setName(java.lang.String name) {
      this.name = name;
      return this;
    }

    public Builder<T> set(String key, String value) {
      if (properties == null) {
        properties = new HashMap<>();
      }

      properties.put(key, value);
      return this;
    }

    public Builder<T> setRequired() {
      this.required = true;
      return this;
    }

    public Builder<T> setUnique() {
      this.unique = true;
      return this;
    }

    public Builder<T> addValidator(String check, String message) {
      if (validators == null) {
        validators = new ArrayList<>();
      }

      validators.add(new Validator(check, message));
      return this;
    }

    public Builder<T> setCaption(java.lang.String caption) {
      this.caption = caption;
      return this;
    }

    public Builder<T> setMinimumLength(int length) {
      this.minimumLength = length;
      return this;
    }

    public Builder<T> setMaximumLength(int length) {
      this.maximumLength = length;
      return this;
    }

    public Builder<T> setLowThreshold(T value) {
      this.lowThreshold = value;
      return this;
    }

    public Builder<T> setHighThreshold(T value) {
      this.highThreshold = value;
      return this;
    }


    /**
     * Set cache size for the column
     * @param limit The number of records to store in cache
     * @return self chaining
     */
    public Builder<T> setCacheLimit(int limit) {
      this.cacheLimit = limit;
      return this;
    }

    public Builder<T> setDefaultValue(T value) {
      this.defaultValue = value;
      return this;
    }


    public Builder<T> allowNull() {
      this.allowNull = true;
      return this;
    }

    /**
     * The field name for the column
     * @return string
     */
    java.lang.String getName() {
      return name;
    }

    /**
     * The number of records to be stored in cache
     * @return 0 if cache is disabled otherwise cache size
     */
    int getCacheLimit() {
      return cacheLimit;
    }

    T getDefaultValue() {
      return defaultValue;
    }

    public boolean canBeNull() {
      return allowNull;
    }
  }

  public static class Text<M extends Model> extends Column<M, java.lang.String> {

    public Text() {
      super(java.lang.String.class);
    }

    public Text(Builder<String> builder) {
      super(java.lang.String.class, builder);
    }

    public String getTypeText() {
      return "TEXT" + super.getLengthLimit();
    }
  }

  public static class Int<M extends Model> extends Column<M, java.lang.Integer> {
    public Int() {
      super(java.lang.Integer.class);
    }

    public Int(Builder<Integer> builder) {
      super(java.lang.Integer.class, builder);
    }

    public String getTypeText() {
      return "INT" + super.getThreshold();
    }
  }

  public static class BigInt<M extends Model> extends Column<M, java.lang.Long> {
    public BigInt() { super(java.lang.Long.class); }

    public BigInt(Builder<java.lang.Long> builder) {
      super(java.lang.Long.class, builder);
    }

    public String getTypeText() { return "INT" + super.getThreshold(); }
  }

  public static class Bool<M extends Model> extends Column<M, Boolean> {

    public Bool() {
      super(Boolean.class);
    }


    public Bool(Builder<Boolean> builder) {
      super(Boolean.class, builder);
    }

    public String getTypeText() {
      return "BOOL";
    }
  }

  public static class Double<M extends Model> extends Column<M, java.lang.Double> {

    public Double() {
      super(java.lang.Double.class);
    }

    public Double(Builder<java.lang.Double> builder) {
      super(java.lang.Double.class, builder);
    }

    public String getTypeText() {
      return "NUMBER" + getThreshold();
    }
  }


  public static class Date<M extends Model> extends Column<M, java.util.Date> {
    public Date() {
      super(java.util.Date.class);
    }

    public Date(Builder<java.util.Date> builder) {
      super(java.util.Date.class, builder);
    }

    public String getTypeText() {
      return "DATE" + getThreshold();
    }
  }

  public static class Index<M extends Model> {

    private final boolean unique;
    private final Column<M, ?> columns[];
    private final boolean columnsOrder[];
    private String name;

    @SafeVarargs
    public Index(Column<M, ?> ... columns) {
      this(false, columns);
    }

    @SafeVarargs
    public Index(boolean unique, Column<M, ?> ... columns) {
      this.columns = columns;
      this.unique = unique;
      this.columnsOrder = new boolean[columns.length];
    }

    public Index<M> setName(String name) {
      this.name = name;
      return this;
    }

    public Index<M> setDescending(Column<M, ?> column, boolean descending) {
      for(int i=0; i<columns.length; ++i) {
        if (columns[i] == column) {
          columnsOrder[i] = descending;
        }
      }
      return this;
    }

    public boolean isUnique() {
      return unique;
    }

    public String getName() {
      return name;
    }

    public int getColumnCount() {
      return columns.length;
    }

    public Column<M, ?> getColumn(int arrayIndex) {
      return columns[arrayIndex];
    }

    public boolean isDescending(int arrayIndex) {
      return columnsOrder[arrayIndex];
    }


  }
}