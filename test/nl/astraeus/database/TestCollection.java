package nl.astraeus.database;

import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Info;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 9:45 PM
 */
public class TestCollection extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestCollection");
    }

    @Test
    public void testCollection() {
        db.begin();

        SimpleDao<Company> companyDao = new SimpleDao<Company>(Company.class);

        Company company = new Company("Astraeus BV");

        company.addInfo(new Info("description 1", "info 1"));
        company.addInfo(new Info("description 2", "info 2"));

        companyDao.insert(company);

        db.commit();

        db.begin();

        Company found = companyDao.find(company.getId());

        Assert.assertNotNull(found);
        Assert.assertEquals(company.getInfoLines().size(), 2);

        db.rollback();

        db.begin();

        company.addInfo(new Info("description 3", "info 3"));

        companyDao.update(company);

        db.commit();

        db.begin();

        found = companyDao.find(company.getId());

        Assert.assertNotNull(found);
        Assert.assertEquals(company.getInfoLines().size(), 3);

        db.rollback();

        db.begin();

        SimpleDao<Info> infoDao = new SimpleDao<Info>(Info.class);

        Info infox = company.getInfoLines().remove(1);

        infoDao.delete(infox);
        companyDao.update(company);

        db.commit();

        db.begin();

        found = companyDao.find(company.getId());

        Assert.assertNotNull(found);
        Assert.assertEquals(company.getInfoLines().size(), 2);

        db.rollback();
    }

}
