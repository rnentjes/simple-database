package nl.astraeus.database;

import junit.framework.Assert;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Company;
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
public class TestMissingReference extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestMissingReference;DB_CLOSE_DELAY=-1");
    }

    @Test
    public void testMissingReference() {
/*
        final Person person = new Person("Test", 44, "Somewhere");
        final Company company = new Company("Some company");

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                person.setCompany(company);

                insert(person);

                Assert.assertNotNull(person);
                Assert.assertNotNull(person.getCompany());
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                delete(company);
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                Person found = find(Person.class, person.getId());

                Assert.assertNotNull(found);
                Assert.assertNull(found.getCompany());
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                delete(person);
            }
        });
*/
    }

}
