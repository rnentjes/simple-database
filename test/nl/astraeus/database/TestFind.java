package nl.astraeus.database;

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
/*
        Person person = new Person("Rien", 40, "Rozendael");

        Persister.begin();

        Persister.insert(person);
        Long id = person.getId();

        Persister.commit();

        Persister.begin();

        Person p2 = Persister.find(Person.class, id);

        Assert.assertNotNull(p2);
        Assert.assertEquals("Rien", p2.getName());

        Persister.rollback();
*/
    }

}
