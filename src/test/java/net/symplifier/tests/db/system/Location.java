package net.symplifier.tests.db.system;

import net.symplifier.db.Column;
import net.symplifier.db.ModelInstance;
import net.symplifier.db.ModelStructure;
import net.symplifier.db.Relation;

/**
 * Created by ranjan on 7/3/15.
 */
public class Location extends ModelInstance {

  public static final Column.Text<Location> name = new Column.Text<>();


  public static final Relation.HasMany<Location, EventLog> logs = new Relation.HasMany<Location, EventLog>(EventLog.class, EventLog.location);

}
