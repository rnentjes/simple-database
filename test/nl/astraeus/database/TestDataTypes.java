package nl.astraeus.database;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Serialized;
import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestDataTypes {

    @BeforeClass
    public static void createDatabase() {
        DdlMapping.get().setExecuteDDLUpdates(true);

        ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:TestCache", "sa", "");
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

    public static class DataTypes {

        @Id
        public Long id;

        @Length(199)
        public String name;

        @Length(precision = 10, scale = 4)
        public double amount;

        @Length(precision = 12, scale = 6)
        public BigDecimal price;

        public int age;
        public long created;

        public Date lastAccess;

        public boolean bit;

        @Serialized
        public String blub;

    }

    @Test
    public void testCache() {
        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                DataTypes type = new DataTypes();

                type.name = "name";
                type.amount = 6.66;
                type.price = new BigDecimal("7.77");
                type.age = 33;
                type.created = System.currentTimeMillis();
                type.lastAccess = new Date();
                type.blub = "Some serialized string thingy!";
                type.bit = true;

                insert(type);
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                List<DataTypes> types = selectAll (DataTypes.class);

                DataTypes type = types.get(0);

                System.out.println("Name: "+type.name);
                System.out.println("Amount: "+type.amount);
                System.out.println("Price: "+type.price);
                System.out.println("Age: "+type.age);
                System.out.println("Created: "+type.created+" ("+new Date(type.created)+")");
                System.out.println("LastAccess: "+type.lastAccess);
                System.out.println("Blub: "+type.blub);
                System.out.println("Bit: "+type.bit);

                Cache.get().clear();

                types = selectAll (DataTypes.class);

                type = types.get(0);

                System.out.println("Name: "+type.name);
                System.out.println("Amount: "+type.amount);
                System.out.println("Price: "+type.price);
                System.out.println("Age: "+type.age);
                System.out.println("Created: "+type.created+" ("+new Date(type.created)+")");
                System.out.println("LastAccess: "+type.lastAccess);
                System.out.println("Blub: "+type.blub);
                System.out.println("Bit: "+type.bit);
            }
        });


    }


}
