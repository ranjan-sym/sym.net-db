package net.symplifier.tests.db;

import net.symplifier.db.exceptions.DatabaseException;
import net.symplifier.db.Schema;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ranjan on 7/3/15.
 */
public class DBTestCase {
  public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public long REF_DATE_TS;

  public static final int GEN_COUNT = 100;
  public static final int LOG_COUNT = 10000;

  private File tempFile;
  private Schema schema;

  @Before
  public void createSchema() throws IOException, ParseException {

//    Schema schema = new Schema();
//
//
//
//    app.getSchema(MySchema.class).
//
//
//    schema.get(EventModel.class).code
//
//
//
//
//    REF_DATE_TS = DATE_TIME_FORMAT.parse("2000-01-24 16:00:00").getTime();
//    // Delete temporary files that might have been created before
//    tempFile = File.createTempFile("java-test-", ".db");
//    tempFile.deleteOnExit();
//    System.out.println("Using temporary database file - " + tempFile.getAbsolutePath());
//    Schema = new TestSystem(new JDBCDriver("jdbc:sqlite:" + tempFile.getAbsolutePath()));
//    Schema.createAll();
  }

  private Random random = new Random();

  @Test
  public void testModelConsistency() throws DatabaseException {

//    Schema.begin();
//
//    System.out.println("Testing basic data insertion feature");
//    HashMap<Long, Generator> source = new HashMap<>();
//    // Let's include 10 generators
//    for(int i=0; i<GEN_COUNT; ++i) {
//      Generator gen = new Generator("Generator - " + (i+1), 5000 * random.nextDouble());
//      Schema.save(gen);
//      source.put(gen.getId(), gen);
//    }
//
//    // Making sure IDs are uniquely generated
//    assert(source.size() == GEN_COUNT);
//
//    // Let's count and verify the number of generators added
//    assert (Schema.Generator.getAll().toList().size() == GEN_COUNT);
//
//    // Also verify through counting query
//    assert(Schema.Generator.query().getSize() == GEN_COUNT);
//
//    System.out.println("Testing ORDER BY feature as well as verifying data");
//    List<Generator> orderedGens = Schema.Generator.query().orderBy(GeneratorModel.capacity, true).getRows().toList();
//
//    Double prevValue = Double.MAX_VALUE;
//    for(Generator gen:orderedGens) {
//      assert(gen.getCapacity() <= prevValue);
//      prevValue = gen.getCapacity();
//
//      Generator sourceGen = source.get(gen.getId());
//      assert(gen.getCapacity().equals(sourceGen.getCapacity()));
//      assert(gen.getName().equals(sourceGen.getName()));
//    }
//
//    System.out.println("Ordering with multiple columns");
//    orderedGens = Schema.Generator.query().orderBy(LocationModel.name, true).orderBy(GeneratorModel.capacity).getRows().toList();
//    for(Generator gen:orderedGens) {
//      Generator sourceGen = source.get(gen.getId());
//      assert(gen.getCapacity().equals(sourceGen.getCapacity()));
//      assert(gen.getName().equals(sourceGen.getName()));
//    }
//
//    System.out.println("Testing data update feature");
//    for(Generator gen:orderedGens) {
//      gen.setCapacity(-5000 * random.nextDouble());
//      gen.setName("Update - " + gen.getName());
//
//      Schema.save(gen);
//    }
//
//    // Let's check the data update has been successful
//    for(Generator gen:Schema.Generator.query().getRows()) {
//      assert(gen.getName().startsWith("Update - "));
//      assert(gen.getCapacity() <= 0);
//    }
//
//
//
//    //assert(Schema.Generator.query().getSize() == 10);
//
//    // Create 10000 records on the log table
//    // using an existing generator record and creating a
//    // new Event record
//    for(int i=0; i<LOG_COUNT; ++i) {
//      EventLog log = new EventLog();
//
//      // Let's keep the date range between 50 days before and after the reference
//      Date time = new Date(((random.nextInt(100) - 50) * 86400000L) + REF_DATE_TS);
//      // The timestamp is set with Date type and the Description is set with String
//      // for testing
//      log.setTimestamp( time );
//      log.setEvent(new Event("EVNT" + String.format("%05d", i + 1), "Normal", DATE_TIME_FORMAT.format(time), false));
//
//      // Get a random ID take 50% chance of storing a null value here
//      long genId = random.nextInt(GEN_COUNT) - (GEN_COUNT/2);
//      log.setLocation(source.get(genId));
//
//      Schema.save(log);
//    }
//
//    assert(Schema.EventLog.query().getSize() == LOG_COUNT);
//
//
//    System.out.println("Trying out filter on a Date type column");
//    Date start = new Date(REF_DATE_TS - 5 * 86400000L);
//    Date end = new Date(REF_DATE_TS + 5 * 86400000L);
//
//    Query<EventLog> q = Schema.EventLog.query();
//    Filter<EventLog> f = q.filter(EventLogModel.timestamp.greaterThan(start))
//            .and(EventLogModel.timestamp.lessThan(end));
//    q.orderBy(EventLogModel.timestamp);
//
//    int size = q.getSize();
//    assert(size < LOG_COUNT);
//
//
//    int nullCount = 0;
//    // Check if the filtration worked out
//    for(EventLog log:q.getRows()) {
//      assert (log.getTimestamp().compareTo(start) > 0);
//      assert (log.getTimestamp().compareTo(end) < 0);
//      if (log.getLocation() == null) {
//        nullCount += 1;
//      }
//      assert(log.getEvent().getDescription().equals(DATE_TIME_FORMAT.format(log.getTimestamp())));
//    }
//    assert(nullCount > 0);
//
//    // Change the parameter of the query to include non null generators
//    f.and(EventLogModel.location.isNot(null));
//    assert(q.getSize() < size);
//    for(EventLog log:q.getRows()) {
//      assert(log.getLocation() != null);
//    }
//
//    // Finally let's try if the limit works or not
//    q.limit(1, 5);
//    // SELECT COUNT(*) does not work with limit
//    assert(q.getRows().toList().size() == 5);
//
//
//    Schema.commit();


  }
}
