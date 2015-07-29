package net.symplifier.db;

import net.symplifier.db.exceptions.ModelException;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by ranjan on 7/28/15.
 */
class ModelScheme<T extends Model> {

  private final ModelScheme<? extends Model> parent;
  private final Set<ModelScheme<? extends Model>> children = new HashSet<>();

  private final Class<T> modelClass;

  /* The factory to create the Model on the fly */
  private final Model.Factory<T> factory;

  /* The columns on a model */
  private final Map<String, Column> columns = new HashMap<>();

  /* Cache of model instances */
  private final WeakHashMap<Long, ModelInstance> cache = new WeakHashMap<>();

  /**
   * Creates a ModelScheme that holds all the information required for
   * creating a Model and manipulating it
   *
   * @param factory The {@link net.symplifier.db.Model.Factory} instance
   *                capable of creating a Model instance
   * @param clazz   The Model class
   */
  ModelScheme(Schema schema, Model.Factory<T> factory, Class<T> clazz) {
    this.modelClass = clazz;
    this.factory = factory;

    // Let's see if this is a root model or if there is some ancestor that can
    // be the root
    Class<? extends Model> parent = (Class<? extends Model>)clazz.getSuperclass();
    this.parent = (parent == Model.class) ? null : schema.find(parent);

    // if this scheme has a parent, then it means it must be a children of that
    // scheme. This feature would be useful in normalizing the models from
    // cached data
    if (this.parent != null) {
      this.parent.children.add(this);
    }


    // Let's find out all the columns defined on this class by reflection
    java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
    for(java.lang.reflect.Field field:fields) {
      // Search for all the fields of type columns
      if (field.getType() != Column.class) {
        continue;
      }

      int modifiers = field.getModifiers();
      // Column field must be declared static
      if (!Modifier.isStatic(modifiers)) {
        throw new ModelException(clazz, "Column field " + field + " is not static");
      }

      // Column field must be declared final
      if(!Modifier.isFinal(modifiers)) {
        throw new ModelException(clazz, "Column field " + field + " is not final");
      }

      try {
        // Get the column instance
        Column col = (Column)field.get(clazz);

        // See if the name given to the column is already used
        if (columns.containsKey(col.getName())) {
          throw new ModelException(clazz, "Column field " + field
                  + " uses name " + col.getName()
                  + " which is already used for " + columns.get(col.getName()));
        }

        // Everything is OK, we got a column
        columns.put(col.getName(), col);
      } catch(IllegalAccessException e) {
        // Column field must be accessible
        throw new ModelException(clazz, "Column field " + field + " is not accessible");
      }
    }
  }


  Class<? extends Model> getModelClass() {
    return modelClass;
  }

  /**
   * Retrieve a column with the given name. This name is not the property name
   * used while declaring the Column object but rather the name used in the
   * database table for declaring this column
   *
   * @param name The name of the column in the database table
   * @return A {@link Column} instance corresponding to the given name
   */
  Column getColumn(String name) {
    return columns.get(name);
  }


  T createInstance(Schema schema) {
    T instance = factory.create(schema);



    return instance;
  }

}
