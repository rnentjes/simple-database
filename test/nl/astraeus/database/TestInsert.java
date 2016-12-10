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
public class TestInsert extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestInsert");
    }

    @Test
    public void testInsert() {
/*
        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                insert(new Person("Rien", 40, "Rozendael"));
                insert(new Person("Jan", 32, "Straat"));
                insert(new Person("Piet", 26, "Weg"));
                insert(new Person("Klaas", 10, "Pad"));
            }
        });

        Persister.begin();

        List<Person> persons = Persister.selectAll(Person.class);

        Assert.assertTrue(persons.size() == 4);

        Persister.rollback();
*/
    }

}
