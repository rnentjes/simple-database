package nl.astraeus.database;

import junit.framework.Assert;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Info;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Date: 11/16/13
 * Time: 9:45 PM
 */
public class TestCollection {

    @BeforeClass
    public static void createDatabase() {
        DdlMapping.get().setExecuteDDLUpdates(true);

        ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestCollection", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    @AfterClass
    public static void clearMetaData() {
        Persister.dispose();
    }

    @Test
    public void testCollection() {
        Persister.begin();

        Company company = new Company("Astraeus BV");

        company.addInfo(new Info("description 1", "info 1"));
        company.addInfo(new Info("description 2", "info 2"));

        Persister.insert(company);

        Persister.commit();

        Persister.begin();

        Company found = Persister.find(Company.class, company.getId());

        Assert.assertNotNull(found);
        Assert.assertEquals(company.getInfoLines().size(), 2);

        Persister.rollback();

        Persister.begin();

        company.addInfo(new Info("description 3", "info 3"));

        Persister.update(company);

        Persister.commit();

        Persister.begin();

        found = Persister.find(Company.class, company.getId());

        Assert.assertNotNull(found);
        Assert.assertEquals(company.getInfoLines().size(), 3);

        Persister.rollback();

        Persister.begin();

        Info infox = company.getInfoLines().remove(1);

        Persister.delete(infox);
        Persister.update(company);

        Persister.commit();

        Persister.begin();

        found = Persister.find(Company.class, company.getId());

        Assert.assertNotNull(found);
        Assert.assertEquals(company.getInfoLines().size(), 2);

        Persister.rollback();
    }

}
