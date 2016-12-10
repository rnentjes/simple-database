package nl.astraeus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import nl.astraeus.database.jdbc.ConnectionProvider;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
@Ignore
public class BaseTest {

    protected static SimpleDatabase db;

    @BeforeClass
    public static void createDatabase(final String url) {
        db = SimpleDatabase.define(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection(url, "sa", "");
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
    }

    @AfterClass
    public static void clearMetaData() {
        db.dispose();
    }

}
