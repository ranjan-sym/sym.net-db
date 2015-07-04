package net.symplifier.tests.db;

import net.symplifier.db.DatabaseException;
import net.symplifier.db.jdbc.JDBCDriver;
import net.symplifier.tests.db.system.GeneratorModel;
import net.symplifier.tests.db.system.TestSystem;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * Created by ranjan on 7/3/15.
 */
public class DBTestCase {

  @Test
  public void testModelConsistency() throws DatabaseException {
    TestSystem Schema = new TestSystem(new JDBCDriver("jdbc:sqlite:test.db"));

    Schema.createAll();

    long start = System.currentTimeMillis();
//    SCHEMA.begin();
//
//    for(int i=0; i<100000; ++i) {
//
//      Generator generator = new Generator();
//      generator.setCapacity(2500.0 * (i%2+1));
//      generator.setName("Unit " + i);
//
//      SCHEMA.save(generator);
//    }
//    SCHEMA.commit();

    //RowIterator<Generator> generators = SCHEMA.Generator.getAll();
    Generator g = Schema.Generator.find(1900);
    System.out.println(g);

    // Let's store some Event Logs
    EventLog eventLog = new EventLog();

    eventLog.setTimestamp(new Date());
    eventLog.setEvent(new Event("START", "Normal", "System Start", null));
    eventLog.setLocation(new Generator("Generator 1", 4000.0));

    Schema.save(eventLog);

    System.out.println("Event log id = " + eventLog.getId());
    eventLog = Schema.EventLog.find(6);

    System.out.println("Event log id = " + eventLog.getId() + " location = " + eventLog.getLocation().getName() + ", " + eventLog.getTimestamp().toString());

    //System.out.println("Event log id = " + eventLog.getId());


    int size = Schema.Generator.query().filter(GeneratorModel.KEY.lessThan(1900L)).query().getSize();
    System.out.println("Number of records =  " + size);

    //List<Generator> generators = Schema.Generator.query().filter(GeneratorModel.KEY.lessThan(1900L)).query().getRows().toList();
    //System.out.println("Count = " + generators.size());

//    for(Generator generator:generators) {
//      System.out.println(generator);
//    }
//    generators = SCHEMA.Generator.getAll().toList();
//    System.out.println("Count = " + generators.size());

    //Event event = SCHEMA.Event.find(1);


    System.out.println("Time taken: " + (System.currentTimeMillis() - start));
    //System.out.println(event.getId());

    //event.setCode("END");
    //SCHEMA.save(event);


  }
}
