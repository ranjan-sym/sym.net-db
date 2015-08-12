package net.symplifier.db.core.field;

import com.sun.org.apache.bcel.internal.generic.FieldGen;
import net.symplifier.db.Field;
import net.symplifier.db.core.field.Getter;
import net.symplifier.db.core.field.Setter;

/**
 * Factory Pattern for creating fields by the Driver
 *
 * Created by ranjan on 7/31/15.
 */
public interface FieldGenerator<T extends Field> {
  T generateField();
}
