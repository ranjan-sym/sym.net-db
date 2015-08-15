package net.symplifier.db;

/**
 * The columns and relations that make up the model. Used by {@link ModelStructure}
 * during initialization
 *
 * Created by ranjan on 8/15/15.
 */
public interface ModelComponent<T extends Model> {

  /**
   * Event called up on the component during its structure initialization
   *
   * @param structure The structure to which this component belongs
   */
  void onInit(ModelStructure<T> structure);
}
