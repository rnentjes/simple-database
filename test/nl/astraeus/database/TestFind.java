package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestFind extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestFind");
    }

    @Test
    public void testFind() throws InterruptedException {
        Person person = new Person("Rien", 40, "Rozendael");

        db.begin();

        personDao.insert(person);

        Long id = person.getId();

        db.commit();

        Person p2 = personDao.find(id);

        Assert.assertNotNull(p2);
        Assert.assertEquals("Rien", p2.getName());
    }

}
