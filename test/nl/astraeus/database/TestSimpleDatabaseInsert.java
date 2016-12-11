package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestSimpleDatabaseInsert extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestInsert");
    }

    @Test
    public void testInsert() {
        SimpleDatabase db = SimpleDatabase.get();

        createPersons();

        List<Person> persons = personDao.selectAll();

        Assert.assertTrue(persons.size() == 5);
    }

}
