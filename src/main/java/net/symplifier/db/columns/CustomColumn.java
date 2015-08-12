package net.symplifier.db.columns;

import net.symplifier.db.Field;
import net.symplifier.db.Model;
import net.symplifier.db.core.field.FieldGenerator;
import net.symplifier.db.core.field.Getter;
import net.symplifier.db.core.field.Setter;

/**
 * A CustomColumn provides a mechanism for declaring and using columns that are
 * not readily available on all drivers. Some drivers may choose to implement
 * the custom column while others may not. In such cases, the CustomColumn
 * provides a Compatibility options to fallback to the Generic field
 *
 * @param <M> The type of the Model to which this column belongs
 * @param <G> The Generic (Primitive) type for compatibility
 * @param <T> The Custom Field for which the Column is defined
 *
 * Created by ranjan on 7/31/15.
 */
public abstract class CustomColumn<M extends Model, G, T extends CustomColumn.CustomFieldType<G>> extends Column<M, T> {

  protected CustomColumn(String name, Class<M> modelType) {
    super(name, modelType);
  }

  public abstract Class<G> getGenericType();

  public static class CompatibilityFieldGenerator implements FieldGenerator<Field> {
    private final FieldGenerator<Field> genericFieldGenerator;

    public CompatibilityFieldGenerator(FieldGenerator genericFieldGenerator) {
      this.genericFieldGenerator = genericFieldGenerator;
    }

    @Override
    public Field generateField() {
      return new CompatibilityField(genericFieldGenerator.generateField());
    }
  }

  public static class CompatibilityField<G, T extends CustomColumn.CustomFieldType<G>> implements Field<T, Object, Getter<Object>, Setter<Object>> {
    private final Field genericField;
    private T obj;

    public CompatibilityField(Field genericField) {
      this.genericField = genericField;
    }

    @Override
    public void apply(Getter getter, Object reference) {
      genericField.apply(getter, reference);
    }

    @Override
    public void apply(Setter setter, Object reference) {
      genericField.apply(setter, reference);
    }

    @Override
    public T get() {
      obj.set((G)genericField.get());
      return null;
    }

    @Override
    public void set(T value) {
      obj = value;
      genericField.set(obj.get());
    }
  }

  public static abstract class CustomFieldType<G> {
    protected G genericValue;

    public CustomFieldType(G value) {
      this.genericValue = value;
    }

    public void set(G value) {
      genericValue = value;
    }

    public G get() {
      return genericValue;
    }

  }
}
