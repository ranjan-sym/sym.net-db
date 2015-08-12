package net.symplifier.db.columns;

import net.symplifier.db.Field;
import net.symplifier.db.Model;
import net.symplifier.db.Schema;
import net.symplifier.db.core.field.FieldGenerator;
import net.symplifier.db.query.Query;
import net.symplifier.db.query.filter.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The Column object represents the Column definition of a model. It holds the
 * key information about the fields of a model, knows how to generate each and
 * every field of a Record based on its Schema.
 *
 *
 * Created by ranjan on 7/27/15.
 */
public abstract class Column<M extends Model, T> implements FilterEntity {

  /* The name of the column as defined in the data structure */
  private final String name;
  private final Class<M> modelType;

  /* Flag to set the field to be immutable */
  private boolean immutable;

  /*
     In most of the cases, there is only a single Schema in an application, so
     as an optimization effort, a column keeps track of a primary schema
     separately, but is also able to work even when multiple schemas are
     defined in the application. The following three properties make this
     possible
   */
  private Schema primarySchema;
  private FieldGenerator primarySchemaFieldGenerator;
  private final Map<Schema, FieldGenerator> fieldGenerators = new HashMap<>();

  private Query<M> findQuery;
  private FilterParameter<T> findParameter;

  protected Column(String name, Class<M> modelType) {
    this.name = name;
    this.modelType = modelType;
  }

  public void addSchema(Schema schema) {
    FieldGenerator generator = immutable ?
              schema.getDriver().getImmutableFieldGenerator(getType()) :
              schema.getDriver().getFieldGenerator(getType());

    // If a field generator could not be found for the given type
    // then its time to fallback to generic compatibility mode
    if (generator == null) {
      // Let's make sure we are dealing with a Custom column
      if (this instanceof CustomColumn) {
        CustomColumn custom = (CustomColumn)this;

        // Looks like we are dealing with a custom field type and the driver doesn't
        // provide a suitable generator, time to fall back to the generic type

        generator = immutable ?
                schema.getDriver().getImmutableFieldGenerator(custom.getGenericType()) :
                schema.getDriver().getFieldGenerator(custom.getGenericType());

        // encapsulate the generic generator within the compatibility generator
        generator = new CustomColumn.CompatibilityFieldGenerator(generator);

      }
    }
    if (primarySchema == null) {
      primarySchema = schema;
      primarySchemaFieldGenerator = generator;
    } else {
      fieldGenerators.put(schema, generator);
    }
  }

  public Field<T, ?, ?, ?> createField(Schema schema) {
    if (schema == primarySchemaFieldGenerator) {
      return primarySchemaFieldGenerator.generateField();
    } else {
      return fieldGenerators.get(schema).generateField();
    }
  }

  public abstract Class<T> getType();


  public String getName() {
    return name;
  }

  public String getQueryText() {
    return name;
  }

  public M find(T value) {
    return null;
  }

  private Filter<M> relation(Operator operator) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(operator);
    return filter;
  }

  private Filter<M> relation(Operator operator, FilterEntity param) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(operator);
    filter.append(param);
    return filter;
  }

  public Filter<M> is(T value) {
    //return is(new FilterParameter<T>(createField()))
    return relation(Operator.eq, new FilterParameter<>(value));
  }
//
//  public Filter<M> is(FilterParameter<T> param) {
//    return relation(Operator.eq, param);
//  }

  public Filter<M> isNot(T value) {
    return relation(Operator.notEq, new FilterParameter<>(value));
  }

  public Filter<M> isNull() {
    return relation(Operator.isNull);
  }

  public Filter<M> notNull() {
    return relation(Operator.notNull);
  }

  public Filter<M> like(T pattern) {
    return relation(Operator.like, new FilterParameter<>(pattern));
  }

  public Filter<M> lessThan(T value) {
    return relation(Operator.lessThan, new FilterParameter<>(value));
  }

  public Filter<M> lessThanEqual(T value) {
    return relation(Operator.lessThenEq, new FilterParameter<>(value));
  }

  public Filter<M> greaterThan(T value) {
    return relation(Operator.greaterThan, new FilterParameter<>(value));
  }

  public Filter<M> greaterThanEqual(T value) {
    return relation(Operator.greaterThanEq, new FilterParameter<>(value));
  }

  public Filter<M> between(T r1, T r2) {
    Filter<M> filter = new Filter<>();
    filter.append(this);
    filter.append(Operator.greaterThanEq);
    filter.append(new FilterParameter<>(r1));
    filter.append(Operator.and);
    filter.append(Operator.lessThan);
    filter.append(new FilterParameter<>(r2));
    return filter;
  }

//  public Filter<M> between(Parameter<T> p1, Parameter<T> p2) {
//    Filter<M> filter = new Filter<>();
//    filter.append(this);
//    filter.append(Operator.greaterThanEq);
//    filter.append(p1);
//    filter.append(Operator.and);
//    filter.append(Operator.lessThan);
//    filter.append(p2);
//    return filter;
//  }

  public synchronized M find(Schema schema, T value, boolean normalized) {
    if (findQuery == null || findQuery.isNormalized() != normalized || findQuery.getSchema() != schema) {
      findParameter = new FilterParameter<>();

      //findQuery = schema.query(modelType, normalized).where(this.is(findParameter));
    } else {
      //findParameter.setValue(value);
    }

    findQuery.execute();
    if(findQuery.next()) {
      // create an instance of the model, update and return it here
      M model = schema.createModel(modelType);
      findQuery.update(model);
      return model;
    } else {
      return null;
    }
  }

}
