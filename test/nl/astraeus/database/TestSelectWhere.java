package nl.astraeus.database;

import java.util.List;

import nl.astraeus.database.test.model.Person;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestSelectWhere extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestSelectWhere");
    }

    @Test
    public void testSelectWhere() {
        createPersons();

        List<Person> persons = personDao.where("age > ?", 30);

        Assert.assertEquals(persons.size(), 3);

        persons = personDao.where("name like ?", "R%");

        Assert.assertEquals(persons.size(), 2);
    }

}
