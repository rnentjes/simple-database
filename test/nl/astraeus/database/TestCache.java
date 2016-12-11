package nl.astraeus.database;

import junit.framework.Assert;

import java.util.List;
import java.util.Map;

import nl.astraeus.database.cache.ObjectCache;
import nl.astraeus.database.test.model.Person;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestCache extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestCache");
    }

    @Test
    public void testCache() {
        Map<Class<?>, ObjectCache<?>> cache = db.getCache().getCache();

        Assert.assertNotNull(cache);
        Assert.assertNotNull(db.getCache().getObjectCache(Person.class));

        db.getCache().getObjectCache(Person.class).setMaxSize(6);

        SimpleDao<Person> dao = new SimpleDao<>(Person.class);

        dao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                dao.insert(new Person("Rien", 40, "Rozendael"));
                dao.insert(new Person("Jan", 32, "Straat"));
                dao.insert(new Person("Piet", 26, "Weg"));
                dao.insert(new Person("Klaas", 10, "Pad"));
                dao.insert(new Person("Rien", 40, "Rozendael"));
                dao.insert(new Person("Jan", 32, "Straat"));
                dao.insert(new Person("Piet", 26, "Weg"));
                dao.insert(new Person("Klaas", 10, "Pad"));
            }
        });


        dao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                List<Person> persons = dao.all();

                Assert.assertEquals(db.getCache().getObjectCache(Person.class).getNumberCached() ,6);
            }
        });
    }

}
