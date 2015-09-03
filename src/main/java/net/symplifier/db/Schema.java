package net.symplifier.db;

import net.symplifier.db.annotations.Table;
import net.symplifier.db.exceptions.DatabaseException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The central class for Database management
 *
 * Created by ranjan on 7/27/15.
 */
public class Schema {
  public static final SimpleDateFormat ISO_8601_DATE_TIME
          = new SimpleDateFormat("YYYY-MM-DD'T'HH:mm:ss'Z'") {{
    this.setTimeZone(TimeZone.getTimeZone("UTC"));
  }};



  public interface Generator {

    Driver buildDriver(Schema schema);

    void initialize(Schema schema);
  }

  private static final Schema primarySchema = new Schema();

  /** The complete list of all the models registered on this schema */
  private final Map<Class<? extends Model>, ModelStructure<? extends Model>> allModels = new LinkedHashMap<>();
  private final Map<String, ModelStructure<ModelIntermediate>> intermediateModels = new LinkedHashMap<>();

  /** A mapping of the models by the name to its corresponding class */
  private final Map<String, Class<? extends Model>> namedModels = new HashMap<>();

  /** The driver to be used by this schema */
  private Driver driver;

  private Schema() {

  }

  public static Schema generate(Generator generator) {
    return generate(generator, true);
  }

  public static Schema generate(Generator generator, boolean primary) {
    Schema schema;
    if (primary) {
      schema = primarySchema;
    } else {
      schema = new Schema();
    }

    if (schema.driver == null) {
      schema.driver = generator.buildDriver(schema);
      // First stage, register all the models and build their structure
      generator.initialize(schema);

      // Second stage, build the relationship
      schema.buildRelationship();
    } else {
      throw new DatabaseException("Trying to initialize already initialized schmea", null);
    }

    return schema;
  }

  private void buildRelationship() {
    for(ModelStructure m:allModels.values()) {
      m.buildRelationship();
    }
  }
  /**
   * Creates all the model into the database system
   */
  public void create() {
    Collection<ModelStructure<? extends Model>> models = allModels.values();

    models.forEach(driver::createModel);

    Collection<ModelStructure<ModelIntermediate>> intermediates = intermediateModels.values();
    intermediates.forEach(driver::createModel);

  }


  @SuppressWarnings("unchecked")
  public static <T extends Model> T get(Class<T> modelClass) {
    return primarySchema.createModel(modelClass);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Model> T get(Class<T> clazz, long id) {
    return primarySchema.find(clazz, id);
  }

  public Driver getDriver() {
    return driver;
  }

  public <T extends Model> ModelStructure<T> registerModel(Class<T> clazz) {
    return registerModel(clazz, new Model.DefaultFactory<>(clazz));
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> ModelStructure<T> getModelStructure(Class clazz) {
    return (ModelStructure<T>)allModels.get(clazz);
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> ModelStructure<T> getModelStructure(String name) {
    return (ModelStructure<T>)allModels.get(namedModels.get(name));
  }
  /**
   * Registers the given Model class with the schema with a custom Model
   * Factory
   *
   * @param clazz The Model class to be registered
   * @param factory The Model factory to be used to generate the model instance
   * @param <T> The type of the Model
   */
  public <T extends Model> ModelStructure<T> registerModel(Class<T> clazz, Model.Factory<T> factory) {
    assert(factory != null);
    return doRegisterModel(clazz, factory);
  }

  <T extends Model> ModelStructure<T> doRegisterModel(Class<T> clazz, Model.Factory<T> factory) {

    @SuppressWarnings("unchecked")
    ModelStructure<T> impl = (ModelStructure<T>)allModels.get(clazz);

    if (impl != null) {
      return impl;
    }

    impl = new ModelStructure<>(this, clazz, factory);
    allModels.put(clazz, impl);

    // Let's find out the name of the model as used in the database system
    Table table = clazz.getAnnotation(Table.class);
    String name = table==null?toDBName(clazz.getSimpleName()):table.value();

    namedModels.put(name, clazz);

    return impl;
  }

  public ModelStructure<ModelIntermediate> getIntermediateModel(String tbl) {
    return intermediateModels.get(tbl);
  }

  public <U extends Model, V extends Model> ModelStructure<ModelIntermediate> registerIntermediateModel(String tbl, Class<U> modelU, Class<V> modelV) {
    ModelStructure<ModelIntermediate> m = intermediateModels.get(tbl);
    if (m == null) {
      m = new ModelStructure<>(this, ModelIntermediate.class, tbl, modelU, modelV);
      intermediateModels.put(tbl, m);
    }

    return m;
  }

  /**
   * Get the primary schema
   *
   * @return {@link Schema}
   */
  public static Schema get() {
    return primarySchema;
  }

  public <T extends Model> Query.Builder<T> query(Class<T> modelClass, Column<T, ?> ... columns) {
    return query(this.getModelStructure(modelClass), columns);
  }

  public <T extends Model> Query.Builder<T> query(ModelStructure<T> modelStructure) {
    return new Query.Builder<>(modelStructure);
  }

  public <T extends Model> Query.Builder<T> query(ModelStructure<T> modelStructure, Column<T, ?> ... columns) {
    return new Query.Builder<>(modelStructure, columns);
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> T createModel(Class<T> modelClass) {
    ModelStructure<T> s = (ModelStructure<T>)allModels.get(modelClass);
    return s.create();
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> T find(Class<T> modelClass, long id) {
    ModelStructure<T> s = (ModelStructure<T>)allModels.get(modelClass);
    return s.get(id);
  }


  // interceptor implementation
  private class InterceptorMap {
    private Map<Class, Set<Interceptor>> interceptors = new HashMap<>();
    public void add(Class clazz, Interceptor interceptor) {
      Set<Interceptor> set = interceptors.get(clazz);
      if (set == null) {
        set = new HashSet<>();
        interceptors.put(clazz, set);
      }

      if (!set.contains(interceptor)) {
        set.add(interceptor);
      }
    }

    public void remove(Class clazz, Interceptor interceptor) {
      Set<Interceptor> set = interceptors.get(clazz);
      if (set != null) {
        set.remove(interceptor);
      }
    }

    public void fireInsert(Class clazz, long id) {
      Set<Interceptor> set = interceptors.get(clazz);
      if (set != null) {
        for(Interceptor i:set) {
          i.onInsert(Schema.this, clazz, id);
        }
      }
    }

    public void fireUpdate(Class clazz, long id) {
      Set<Interceptor> set = interceptors.get(clazz);
      if (set != null) {
        for(Interceptor i:set) {
          i.onUpdate(Schema.this, clazz, id);
        }
      }
    }

    public void fireDelete(Class clazz, long id) {
      Set<Interceptor> set = interceptors.get(clazz);
      if (set != null) {
        for(Interceptor i:set) {
          i.onDelete(Schema.this, clazz, id);
        }
      }
    }
  }

  private InterceptorMap insertInterceptors = new InterceptorMap();
  private InterceptorMap updateInterceptors = new InterceptorMap();
  private InterceptorMap deleteInterceptors = new InterceptorMap();

  public <T extends Model> void addInterceptor(Class<T> clazz, Interceptor interceptor, int flags) {
    if ( (flags & Interceptor.INSERT) != 0) {
      insertInterceptors.add(clazz, interceptor);
    }
    if ( (flags & Interceptor.UPDATE) != 0) {
      updateInterceptors.add(clazz, interceptor);
    }
    if ( (flags & Interceptor.DELETE) != 0) {
      deleteInterceptors.add(clazz, interceptor);
    }
  }

  public <T extends Model> void removeInterceptor(Class<T> clazz, Interceptor interceptor) {
    insertInterceptors.remove(clazz, interceptor);
    updateInterceptors.remove(clazz, interceptor);
    deleteInterceptors.remove(clazz, interceptor);
  }

  public void fireInsertInterceptors(ModelRow row) {
    insertInterceptors.fireInsert(row.getStructure().getType(), row.getId());
  }

  public void fireUpdateInterceptors(ModelRow row) {
    updateInterceptors.fireUpdate(row.getStructure().getType(), row.getId());
  }

  public void fireDeleteInterceptors(ModelRow row) {
    deleteInterceptors.fireDelete(row.getStructure().getType(), row.getId());
  }


//
//  private final ThreadLocal<Session> session = new ThreadLocal<Session>() {
//    @Override
//    protected Session initialValue() {
//      return driver.createSession();
//    }
//  };


  /* Stock of all the models that belong to this schema */
  //private final Map<Class<? extends Row>, Model<? extends Row>> models = new HashMap<>();
  //private final HashMap<Class, Model> models = new HashMap<>();


//  /**
//   * Register the given model on this schema. If the model is already registered,
//   * we will try to check if the existing model is consistent with the given
//   * model trying to register, in which case the already registered model is
//   * returned. If the already registered model is not consistent, throw an exception
//   * @param clazz
//   * @param <T>
//   * @return
//   */
//  public <T extends Model> T registerModel(Class<T> clazz) throws ModelException {
//    // automatically detect the name of the model table name
//    String className = clazz.getSimpleName();
//    if (className.endsWith("Model")) {
//      className = className.substring(0, className.length()-5);
//    }
//    return registerModel(clazz, toDBName(className));
//
//  }
//
//  public <T extends Model> T registerModel(Class<T> clazz, String name) throws ModelException {
//    synchronized(models) {
//      // let's see if we have the model already registered
//      Model m = models.get(clazz);
//      if (m != null) {
//        return (T)m;
//      }
//
//      try {
//        T model = clazz.newInstance();
//        models.put(clazz, model);
//        model.setName(name);
//        return model;
//      } catch (IllegalAccessException e) {
//        throw new ModelException("The database model constructor is not accessible for " + clazz.getName(), e);
//      } catch (InstantiationException e) {
//        throw new ModelException("The database model doesn't have a pages.private constructor in " + clazz.getName(), e);
//      }
//
//    }
//  }
//
//  public <T extends Model> T get(Class<T> clazz) {
//    return (T)models.get(clazz);
//  }

  public <T extends Model> Query<T> createQuery(Query.Builder<T> builder) {
    return getDriver().createQuery(builder);
  }

  /**
   * Convert the given className to a suitable database table name. The className
   * is assumed to be in TitleCase and the DBName is assumed to be all small
   * with words separated by underscore.
   * <p>
   *   Example: Station -> station, StationParameter -> station_parameter
   * </p>
   * @param className The class name that needs to be converted to db name
   * @return An all small database name
   */
  public static String toDBName(String className) {
    StringBuilder res = new StringBuilder();
    for(int i=0; i<className.length(); ++i) {
      char ch = className.charAt(i);
      if (ch >= 'A' && ch <='Z') {
        if (i > 0) {
          res.append('_');
        }
        res.append((char)(ch + 32));
      } else {
        res.append(ch);
      }
    }
    return res.toString();
  }

}
