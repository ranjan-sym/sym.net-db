package net.symplifier.db;

import net.symplifier.db.annotations.Table;

import java.util.*;

/**
 * The central class for Database management
 *
 * Created by ranjan on 7/27/15.
 */
public class Schema {
  private static Schema primarySchema = null;

  /** The complete list of all the models registered on this schema */
  private final Map<Class<? extends Model>, ModelStructure<? extends Model>> allModels = new HashMap<>();

  /** A mapping of the models by the name to its corresponding class */
  private final Map<String, Class<? extends Model>> namedModels = new HashMap<>();

  /** The driver to be used by this schema */
  private final Driver driver;

  public Schema(Driver driver) {
    assert (driver != null);
    this.driver = driver;
    if (primarySchema == null) {
      primarySchema = this;
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> T get(Class<T> modelClass) {
    return (T)allModels.get(modelClass).create();
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> T get(Class<T> clazz, final long id) {
    final ModelStructure<T> impl = (ModelStructure<T>) allModels.get(clazz);
    return impl.get(id);
  }

  public Driver getDriver() {
    return driver;
  }

  public <T extends Model> ModelStructure<T> registerModel(Class<T> clazz) {
    return registerModel(clazz, new Model.DefaultFactory<>(clazz));
  }

  @SuppressWarnings("unchecked")
  public <T extends Model> ModelStructure<T> getModelStructure(Class<T> clazz) {
    return (ModelStructure<T>)allModels.get(clazz);
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

  public static Schema get() {
    return primarySchema;
  }

  public <T extends Model> Query.Builder<T> query(Class<T> modelClass, Column<T, ?> ... columns) {
    return new Query.Builder<>(this.getModelStructure(modelClass), columns);
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
