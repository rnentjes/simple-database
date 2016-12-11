package nl.astraeus.database;

import java.util.List;

import nl.astraeus.database.test.model.Person;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
@Ignore
public class TestSelectFrom extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestSelectFrom");
    }

    @Test
    public void testSelectFrom() {
        createPersons();

        List<Person> persons = personDao.selectFrom("where age > ?", 30);

        Assert.assertEquals(3, persons.size());

        persons = personDao.selectWhere("name like ?", "R%");

        Assert.assertEquals(2, persons.size());
    }

}
