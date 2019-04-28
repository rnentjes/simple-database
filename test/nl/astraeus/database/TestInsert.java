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
public class TestInsert extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestInsert");
    }

    @Test
    public void testInsert() {
        SimpleDao<Person> dao = new SimpleDao<Person>(Person.class);

        dao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                dao.insert(new Person("Rien", 40, "Road"));
                dao.insert(new Person("Jan", 32, "Straat"));
                dao.insert(new Person("Piet", 26, "Weg"));
                dao.insert(new Person("Klaas", 10, "Pad"));

                Person p = new Person("Klaas 2", 50, "bla");

                p.setNewColumnTest("Very long test we hope...");
                dao.insert(p);
            }
        });

        List<Person> persons = dao.all();

        Assert.assertTrue(persons.size() == 5);

        Person p = dao.find("name = ?", "Klaas 2");

        Assert.assertEquals("Very long test we hope...", p.getNewColumnTest());
    }

}
