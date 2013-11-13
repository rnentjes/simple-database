package nl.astraeus.database;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/13/13
 * Time: 9:28 PM
 */
public class Persister {

    private static ThreadLocal<Transaction> transactions = new ThreadLocal<Transaction>();
    private static Map<Class<?>, ObjectPersister> objectPersisters = new HashMap<>();
    private static DataSource dataSource;


    private static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (Persister.class) {
                if (dataSource == null) {
                    JdbcDataSource jdbcDataSource = new JdbcDataSource();

                    jdbcDataSource.setUser("sa");
                    jdbcDataSource.setPassword("");
                    jdbcDataSource.setURL("jdbc:h2:~/test");

                    dataSource = jdbcDataSource;
                }
            }
        }

        return dataSource;
    }

    public static void begin() {
        try {
            transactions.set(new Transaction(getDataSource().getConnection()));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void commit() {
        if (transactions.get() != null) {
            try {
                transactions.get().getConnection().commit();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                transactions.set(null);
            }
        }
    }

    public static void rollback() {
        if (transactions.get() != null) {
            try {
                transactions.get().getConnection().rollback();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                transactions.set(null);
            }
        }
    }

    public static void execute(Runnable runnable) {
        try {
            begin();

            runnable.run();

            commit();
        } finally {
            if (transactionActive()) {
                rollback();
            }
        }
    }

    public static void store(Object obj) {
        getObjectPersister(obj.getClass()).store(transactions.get().getConnection(), obj);
    }

    private static ObjectPersister getObjectPersister(Class<?> cls) {
        ObjectPersister result = objectPersisters.get(cls);

        if (result == null) {
            synchronized (Persister.class) {
                result = objectPersisters.get(cls);
                if (result == null) {
                    result = new ObjectPersister(cls);

                    objectPersisters.put(cls, result);
                }
            }
        }

        return result;
    }

    public static boolean transactionActive() {
        return transactions.get() != null;
    }

}
