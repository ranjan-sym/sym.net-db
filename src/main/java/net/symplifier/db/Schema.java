package net.symplifier.db;

import net.symplifier.db.annotations.Table;
import net.symplifier.db.exceptions.ModelException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by ranjan on 7/27/15.
 */
public class Schema {

  private final Map<String, ModelScheme<? extends Model>> models =
          new HashMap<>();


  private final Driver driver;

  public Schema(Driver driver) {
    assert(driver != null);

    this.driver = driver;
  }

  /**
   * Registers the given Model class with the schema with a default Factory
   * to generate the model instance
   *
   * @param clazz The Model class to be registered
   * @param <T> The type of the Model
   */
  private <T extends Model> void registerModel(Class<T> clazz) {
    registerModel(clazz, new Model.DefaultFactory<>(clazz));
  }

  /**
   * Registers the given Model class with the schema with a custom Model
   * Factory
   *
   * @param clazz The Model class to be registered
   * @param factory The Model factory to be used to generate the model instance
   * @param <T> The type of the Model
   */
  public <T extends Model> void registerModel(Class<T> clazz, Model.Factory<T> factory) {
    assert(factory != null);
    // registering a model multiple times is not allowed
    if (models.containsKey(clazz)) {
      throw new ModelException(clazz, "Factory already registered");
    }

    // Let's find out the name of the model as used in the database system
    Table table = clazz.getAnnotation(Table.class);
    String name = table==null?toDBName(clazz.getSimpleName()):table.value();

    models.put(name, new ModelScheme<T>(factory, clazz));
  }

  private final ThreadLocal<Session> session = new ThreadLocal<Session>() {
    @Override
    protected Session initialValue() {
      return driver.createSession();
    }
  };

  public <T extends Model> T createModel(Class<T> modelClass) {
    return null;
  }


  public <T extends Model> T createModel(String name) {
    return (T)models.get(name).createInstance(this);
  }


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
//        throw new ModelException("The database model doesn't have a default constructor in " + clazz.getName(), e);
//      }
//
//    }
//  }
//
//  public <T extends Model> T get(Class<T> clazz) {
//    return (T)models.get(clazz);
//  }


  <M extends Model> Query<M> query(Class<M> modelClass) {
    return query(modelClass, false);
  }

  <M extends Model> Query<M> query(Class<M> modelClass, boolean normalized) {
    return null;
  }

  /**
   * Finds a ModelScheme for the given class type
   *
   * @param clazz The Model class whose scheme is to be retrieved
   * @return The ModelScheme for the given class or null
   */
  ModelScheme find(Class<? extends Model> clazz) {
    for(ModelScheme scheme: models.values()) {
      if(scheme.getModelClass() == clazz) {
        return scheme;
      }
    }

    return null;
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

  public <F, M> Field<F> createField(M model, Column<M, F> column) {

  }

  public <F, M> Field<F> createField(M model, Column<M, F> column, F value) {

  }
}
