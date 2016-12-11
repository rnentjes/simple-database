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
public class TestUpdate extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestUpdate");
    }

    @Test
    public void testUpdate() {
        createPersons();

        List<Person> persons = personDao.where("age > ?", 30);

        db.begin();

        for (Person person : persons) {
            person.setAge(person.getAge() + 1);

            personDao.update(person);
        }

        db.commit();

        persons = personDao.where("age > ?", 32);

        Assert.assertEquals(persons.size(), 2);
    }

}
