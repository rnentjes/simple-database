package nl.astraeus.database;

import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Person;
import org.junit.AfterClass;
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
public class TestDelete extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestDelete");
    }

    @Test
    public void testDelete() {
        createPersons();

        final List<Person> persons = personDao.selectWhere("age > ?", 30);

        // assert 2
        Assert.assertEquals(3, persons.size());

        personDao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                for (Person person : persons) {
                    dao.delete(person);
                }
            }
        });

        // assert 2 left
        Assert.assertEquals(2, personDao.selectAll().size());
    }

}
