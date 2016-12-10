package nl.astraeus.database;

import junit.framework.Assert;
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

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestDelete extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestDelete");
    }

    @Test
    public void testDelete() {
/*        Persister.begin();

        Persister.insert(new Person("Rien", 40, "Rozendael"));
        Persister.insert(new Person("Jan", 32, "Straat"));
        Persister.insert(new Person("Piet", 26, "Weg"));
        Persister.insert(new Person("Klaas", 10, "Pad"));

        Persister.commit();

        Persister.begin();

        List<Person> persons = Persister.selectWhere(Person.class, "age > ?", 30);

        Persister.rollback();

        Persister.begin();

        for (Person person : persons) {
            Persister.delete(person);
        }

        Persister.commit();

        Persister.begin();

        persons = Persister.selectAll(Person.class);

        Persister.rollback();

        Assert.assertEquals(persons.size(), 2);*/
    }

}
