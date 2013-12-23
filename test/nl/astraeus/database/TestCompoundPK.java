package nl.astraeus.database;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Date: 11/17/13
 * Time: 12:58 PM
 */
public class TestCompoundPK {
    private final static Logger logger = LoggerFactory.getLogger(TestCompoundPK.class);

    @BeforeClass
    public static void createDatabase() {
        DdlMapping.get().setExecuteDDLUpdates(true);

        ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestCompoundPK", "sa", "");
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

    @Table(name="test")
    public static class CompoundError {
        @Id
        Long id1;

        @Id
        Long id2;
    }

    @Table(name="test2")
    public static class InvalidPrimaryKeyType {
        @Id
        String name;

        Long id2;
        String data;
    }

    @Test(expected = IllegalStateException.class)
    public void testCompoundError() {
        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                insert(new CompoundError());
            }
        });

    }


    @Test(expected = IllegalStateException.class)
    public void testInvalidPrimaryKey() {
        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                insert(new InvalidPrimaryKeyType());
            }
        });

    }
}
