package net.symplifier.db;

/**
 * Created by ranjan on 7/4/15.
 */
public class Reference<T extends Row> {
  private Long id;
  private T ref;
  private Model<T> model;

  public Long getId() {
    // The id has to be retrieved from the reference model if its available.
    // When a new row is referenced and later saved, there is no way to
    // set the id;
    if (id == null && ref != null) {
      return ref.getId();
    } else {
      return id;
    }
  }

  void setId(Model<T> model, Long id) {
    this.id = id;
    this.model = model;
    if (ref != null && (id==null || !id.equals(ref.getId()))) {
      ref = null;
    }
  }

  public void set(T ref) {
    this.ref = ref;
    if (ref != null) {
      id = ref.getId();
    } else {
      id = null;
    }
  }

  public T get() {
    if (ref == null && id != null) {
      try {
        ref = model.find(id);
      } catch(DatabaseException e) {
        return null;
      }
    }
    return ref;
  }
}
