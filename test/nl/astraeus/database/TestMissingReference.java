package nl.astraeus.database;

import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Person;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
        db.begin();

        final Person person = new Person("Test", 44, "Somewhere");
        final Company company = new Company("Some company");

        person.setCompany(company);

        personDao.insert(person);

        Assert.assertNotNull(person);
        Assert.assertNotNull(person.getCompany());

        db.commit();

        db.begin();

        companyDao.delete(company);

        db.commit();

        db.begin();

        Person found = personDao.find(person.getId());

        Assert.assertNotNull(found);
        Assert.assertNull(found.getCompany());
    }

}
