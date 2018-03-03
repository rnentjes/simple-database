package nl.astraeus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Person;

import org.junit.AfterClass;
import org.junit.Ignore;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
@Ignore
public class BaseTest {

    protected static SimpleDatabase db;

    SimpleDao<Person> personDao = new SimpleDao<>(Person.class);
    SimpleDao<Company> companyDao = new SimpleDao<>(Company.class);

    public static void createDatabase(final String url) {
        db = SimpleDatabase.define(new ConnectionPool(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection(url, "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException | SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }));

        db.setExecuteDDLUpdates(true);
    }

    @AfterClass
    public static void clearMetaData() {
        db.dispose();
    }

    void createPersons() {
        personDao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                dao.insert(new Person("Rien", 40, "Road"));
                dao.insert(new Person("Jan", 32, "Straat"));
                dao.insert(new Person("Ronald", 31, "Wherever"));
                dao.insert(new Person("Piet", 26, "Weg"));
                dao.insert(new Person("Klaas", 10, "Pad"));
            }
        });
    }
}
