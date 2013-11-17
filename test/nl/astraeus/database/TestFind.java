package nl.astraeus.database;

import junit.framework.Assert;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.test.model.Person;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestFind {

    @BeforeClass
    public static void createDatabase() {
        ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestFind", "sa", "");
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
    public void testFind() {
        Person person = new Person("Rien", 40, "Rozendael");

        Persister.begin();

        Persister.insert(person);
        Long id = person.getId();

        Persister.commit();

        Person p2 = Persister.find(Person.class, id);

        Assert.assertNotNull(p2);
        Assert.assertEquals("Rien", p2.getName());
    }

}
