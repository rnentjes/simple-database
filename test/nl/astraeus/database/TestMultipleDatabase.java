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
public class TestMultipleDatabase {

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

        firstDao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                dao.insert(new Person("Jan", 32, "Straat"));
                dao.insert(new Person("Ronald", 31, "Wherever"));
                dao.insert(new Person("Piet", 26, "Weg"));
                dao.insert(new Person("Klaas", 10, "Pad"));
            }
        });

        List<Person> persons = firstDao.where("age > ?", 30);

        first.begin();

        for (Person person : persons) {
            person.setAge(person.getAge() + 1);

            firstDao.update(person);
        }

        first.commit();

        persons = firstDao.where("age > ?", 32);

        Assert.assertEquals(persons.size(), 1);

        persons = secondDao.where("age > ?", 32);

        Assert.assertEquals(persons.size(), 0);
    }

}
