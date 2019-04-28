package nl.astraeus.database;

import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Info;
import nl.astraeus.database.test.model.Person;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestReference extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestReference");
    }

    @Test
    public void testReference() {
        db.getCache().clear();

        db.begin();

        Person person = new Person("Name", 40, "Street");
        Company company = new Company("Company name");
        person.setCompany(company);

        companyDao.insert(company);
        personDao.insert(person);

        db.commit();

        db.begin();

        Person p2 = personDao.find(person.getId());

        db.rollback();

        Assert.assertNotNull(p2);
        Assert.assertNotNull(p2.getCompany());
        Assert.assertEquals(p2.getCompany().getName(), "Company name");

        Assert.assertEquals(1, db.getCache().getObjectCache(Person.class).getNumberCached());
        Assert.assertEquals(1, db.getCache().getObjectCache(Company.class).getNumberCached());
    }

    @Test
    public void testReferentList() {
        db.begin();

        Company company = new Company("Company name");
        company.addInfo(new Info("description", "info"));
        company.addInfo(new Info("description", "info"));
        company.addInfo(new Info("description", "info"));
        company.addInfo(new Info("description", "info"));

        companyDao.insert(company);

        db.commit();

        db.begin();

        Company found = companyDao.find(company.getId());

        db.rollback();

        for (Info info : company.getInfoLines()) {
            Assert.assertNotNull(info.getDescription());
            Assert.assertNotNull(info.getInfo());
        }

        Assert.assertNotNull(found);
        Assert.assertNotNull(found.getInfoLines());
        Assert.assertEquals(found.getInfoLines().size(), 4);
    }

}
