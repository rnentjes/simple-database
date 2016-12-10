package nl.astraeus.database;

import junit.framework.Assert;
import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Info;
import nl.astraeus.database.test.model.Person;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
/*
        Cache.get().clear();

        Persister.begin();

        Person person = new Person("Name", 40, "Street");
        Company company = new Company("Company name");
        person.setCompany(company);

        Persister.insert(company);
        Persister.insert(person);

        Persister.commit();

        Persister.begin();

        Person p2 = Persister.find(Person.class, person.getId());

        Persister.rollback();

        Assert.assertNotNull(p2);
        Assert.assertNotNull(p2.getCompany());
        Assert.assertEquals(p2.getCompany().getName(), "Company name");

        Assert.assertEquals(Cache.get().getObjectCache(Person.class).getNumberCached(), 1);
        Assert.assertEquals(Cache.get().getObjectCache(Company.class).getNumberCached(), 1);
*/
    }

    @Test
    public void testReferentList() {
/*
        Persister.begin();

        Company company = new Company("Company name");
        company.addInfo(new Info("description", "info"));
        company.addInfo(new Info("description", "info"));
        company.addInfo(new Info("description", "info"));
        company.addInfo(new Info("description", "info"));

        Persister.insert(company);

        Persister.commit();

        Persister.begin();

        Company found = Persister.find(Company.class, company.getId());

        Persister.rollback();

        for (Info info : company.getInfoLines()) {
            Assert.assertNotNull(info.getDescription());
            Assert.assertNotNull(info.getInfo());
        }

        Assert.assertNotNull(found);
        Assert.assertNotNull(found.getInfoLines());
        Assert.assertEquals(found.getInfoLines().size(), 4);
*/
    }

}
