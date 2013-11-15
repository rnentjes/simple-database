package nl.astraeus.database;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
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

    protected static Connection getConnection() {
        if (transactions.get() == null) {
            throw new IllegalStateException("No transaction active!");
        }

        return transactions.get().getConnection();
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
                try {
                    transactions.get().getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
                try {
                    transactions.get().getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

    public static void insert(Object obj) {
        getObjectPersister(obj.getClass()).insert(obj);
    }

    public static void update(Object obj) {
        getObjectPersister(obj.getClass()).update(obj);
    }

    public static void delete(Object obj) {
        getObjectPersister(obj.getClass()).delete(obj);
    }

    public static <T> T find(Class<T> cls, long id) {
        return (T)getObjectPersister(cls).find(id);
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
