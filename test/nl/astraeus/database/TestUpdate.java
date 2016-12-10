package nl.astraeus.database;

import junit.framework.Assert;
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

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestUpdate extends BaseTest {
    private final static Logger logger = LoggerFactory.getLogger(TestUpdate.class);

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestUpdate");
    }

    @Test
    public void testUpdate() {
/*
        Persister.begin();

        Persister.insert(new Person("Rien", 40, "Rozendael"));
        Persister.insert(new Person("Jan", 32, "Straat"));
        Persister.insert(new Person("Piet", 26, "Weg"));
        Persister.insert(new Person("Klaas", 10, "Pad"));

        Persister.commit();

        Persister.begin();

        List<Person> persons = Persister.selectWhere(Person.class, "age > ?", 30);

        Persister.begin();

        for (Person person : persons) {
            person.setAge(person.getAge() + 1);

            Persister.update(person);
        }

        Persister.commit();

        Persister.begin();

        persons = Persister.selectWhere(Person.class, "age > ?", 32);

        Assert.assertEquals(persons.size(), 2);

        Persister.rollback();
*/
    }

}
