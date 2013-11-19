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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestCache {

    @BeforeClass
    public static void createDatabase() {
        ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestCache", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    @AfterClass
    public static void clearMetaData() {
        Persister.dispose();
    }

    @Test
    public void testCache() {
        Map<Class<?>, ObjectCache<?>> cache = Cache.get().getCache();

        Assert.assertNotNull(cache);
        Assert.assertNotNull(Cache.get().getObjectCache(Person.class));

        Cache.get().getObjectCache(Person.class).setMaxSize(6);

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                insert(new Person("Rien", 40, "Rozendael"));
                insert(new Person("Jan", 32, "Straat"));
                insert(new Person("Piet", 26, "Weg"));
                insert(new Person("Klaas", 10, "Pad"));
                insert(new Person("Rien", 40, "Rozendael"));
                insert(new Person("Jan", 32, "Straat"));
                insert(new Person("Piet", 26, "Weg"));
                insert(new Person("Klaas", 10, "Pad"));
            }
        });

        List<Person> persons = Persister.selectAll(Person.class);

        Assert.assertEquals(Cache.get().getObjectCache(Person.class).getNumberCached() ,6);
    }

}
