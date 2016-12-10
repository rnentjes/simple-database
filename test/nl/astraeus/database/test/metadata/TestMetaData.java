package nl.astraeus.database.test.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import nl.astraeus.database.MetaData;
import nl.astraeus.database.SimpleDao;
import nl.astraeus.database.SimpleDatabase;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Person;

import org.junit.Ignore;

/**
 * Date: 11/13/13
 * Time: 10:18 PM
 */
@Ignore
public class TestMetaData {

    public static void main(String [] args) {
        SimpleDatabase db = SimpleDatabase.define(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:Test", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        db.setExecuteDDLUpdates(true);

        // MetaDataHandler.get().getMetaData(Company.class);
        SimpleDao<Person> dao = new SimpleDao<>(Person.class);

        dao.find(0L);

        MetaData metaData = new MetaData(Person.class, db);

        System.out.println(metaData);

        // Persister.commit();
    }
}
