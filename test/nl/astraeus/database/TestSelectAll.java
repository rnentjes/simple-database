package nl.astraeus.database;

import junit.framework.Assert;
import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.cache.ObjectCache;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Person;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestSelectAll extends BaseTest {
    private final static Logger logger = LoggerFactory.getLogger(TestSelectAll.class);

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestSelectAll");
    }

    @Test
    public void testSelectAll() {
        createPersons();

        List<Person> persons = personDao.selectAll();

        Assert.assertEquals(persons.size(), 5);

        db.getCache().clear();

        long start1 = System.nanoTime();
        persons = personDao.selectAll();
        long stop1 = System.nanoTime();

        for (Person person : persons) {
            logger.info("all Found: " + person.getName());
        }

        long start2 = System.nanoTime();
        persons = personDao.selectAll();
        long stop2 = System.nanoTime();

        for (Person person : persons) {
            logger.info("2all Found: "+person.getName());
        }

        long start3 = System.nanoTime();
        persons = personDao.selectAll();
        long stop3 = System.nanoTime();

        for (Person person : persons) {
            logger.info("3all Found: "+person.getName());
        }

        logger.info("time1 "+(stop1-start1));
        logger.info("time2 "+(stop2-start2));
        logger.info("time3 "+(stop3-start3));

        Map<Class<?>, ObjectCache<?>> cache = db.getCache().getCache();

        for (Class cls : cache.keySet()) {
            logger.info("# Cached "+cls.getSimpleName()+": " + cache.get(cls).getNumberCached());
        }
    }

}
