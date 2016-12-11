package nl.astraeus.database;

import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Person;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestMultipleDatabase extends BaseTest {

    private static SimpleDatabase first;
    private static SimpleDatabase second;

    @BeforeClass
    public static void createDatabase() {
        first = SimpleDatabase.define("first", new ConnectionPool(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestFirst", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException | SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }));

        second = SimpleDatabase.define("second", new ConnectionPool(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestSecond", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException | SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }));

        first.setExecuteDDLUpdates(true);
        second.setExecuteDDLUpdates(true);
    }

    @Test
    public void testUpdate() {
        SimpleDao<Person> firstDao = new SimpleDao<Person>(Person.class, "first");
        SimpleDao<Person> secondDao = new SimpleDao<Person>(Person.class, "second");

        createPersons();

        List<Person> persons = personDao.selectWhere("age > ?", 30);

        db.begin();

        for (Person person : persons) {
            person.setAge(person.getAge() + 1);

            personDao.update(person);
        }

        db.commit();

        persons = personDao.selectWhere("age > ?", 32);

        Assert.assertEquals(persons.size(), 2);
    }

}
