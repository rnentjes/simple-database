package nl.astraeus.database;

import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/13/13
 * Time: 9:28 PM
 */
public class Persister {

    private static ThreadLocal<Transaction> transactions = new ThreadLocal<Transaction>();
    private static Map<Class<?>, ObjectPersister> objectPersisters = new HashMap<>();

    static {
        if (!ConnectionPool.get().hasConnectionProvider()) {
            ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
                @Override
                public Connection getConnection() {
                    try {
                        Class.forName("org.h2.Driver");
                        Class.forName("nl.astraeus.jdbc.Driver");

                        Connection connection = DriverManager.getConnection("jdbc:stat::jdbc:h2:~/test", "sa", "");
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
    }

    protected static Connection getConnection() {
        if (transactions.get() == null) {
            throw new IllegalStateException("No transaction active!");
        }

        return transactions.get().getConnection();
    }

    protected static Connection getNewConnection() {
        return ConnectionPool.get().getConnection();
    }

    public static void begin() {
        transactions.set(new Transaction(ConnectionPool.get().getConnection()));
    }

    public static void commit() {
        if (transactions.get() != null) {
            Connection connection = transactions.get().getConnection();
            try {
                connection.commit();
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                try {
                    connection.close();
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

    public abstract static class Executor {

        protected void store(Object o) {
            Persister.insert(o);
        }

        protected void insert(Object o) {
            Persister.insert(o);
        }

        protected void update(Object o) {
            Persister.update(o);
        }

        protected void delete(Object o) {
            Persister.delete(o);
        }

        protected <T> T find(Class<T> cls, long id) {
            return Persister.find(cls, id);
        }

        protected <T>List<T> selectFrom(Class<T> cls, String query, Object ... params) {
            return Persister.selectFrom(cls, query, params);
        }

        protected <T>List<T> selectAll(Class<T> cls) {
            return Persister.selectAll(cls);
        }

        protected static <T>List<T> selectWhere(Class<T> cls, String query, Object ... params) {
            return Persister.selectWhere(cls, query, params);
        }

        protected static <T> T findWhere(Class<T> cls, String query, Object ... params) {
            return Persister.findWhere(cls, query, params);
        }

        public abstract void execute();

    }

    public static void execute(Executor runnable) {
        try {
            begin();

            runnable.execute();

            commit();
        } finally {
            if (transactionActive()) {
                rollback();
            }
        }
    }

    public static void init(Class cls) {
        MetaDataHandler.get().getMetaData(cls);
    }

    public static void store(Object obj) {
        MetaData meta = MetaDataHandler.get().getMetaData(obj.getClass());

        if (meta != null) {
            Long id = meta.getId(obj);

            if (id == null || id == 0L) {
                insert(obj);
            } else {
                update(obj);
            }
        } else {
            throw new IllegalStateException("No metadata found for class "+obj.getClass().getName());
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
        if (Cache.get().inCache(cls, id)) {
            T result = Cache.get().get(cls, id);

            MetaData<T> meta = MetaDataHandler.get().getMetaData(cls);

            if (result != null) {
                result = meta.clone(result);

                meta.reloadReferences(result);
            }

            return result;
        }

        T result = getObjectPersister(cls).find(id);

        Cache.get().set(cls, id, result);

        return result;
    }

    public static <T>List<T> selectFrom(Class<T> cls, String query, Object ... params) {
        return getObjectPersister(cls).selectFrom(query, params);
    }

    public static <T>List<T> selectAll(Class<T> cls) {
        return getObjectPersister(cls).selectAll();
    }

    public static <T>List<T> selectWhere(Class<T> cls, String query, Object ... params) {
        return getObjectPersister(cls).selectWhere(query, params);
    }

    public static <T>List<T> selectWhere(Class<T> cls, int from, int max, String query, Object ... params) {
        return getObjectPersister(cls).selectWhere(from, max, query, params);
    }

    public static <T> int selectCount(Class<T> cls, String query, Object ... params) {
        return getObjectPersister(cls).selectCount(query, params);
    }

    public static <T> T findWhere(Class<T> cls, String query, Object ... params) {
        return getObjectPersister(cls).findWhere(query, params);
    }

    private static <T> ObjectPersister<T> getObjectPersister(Class<T> cls) {
        ObjectPersister<T> result = objectPersisters.get(cls);

        if (result == null) {
            synchronized (Persister.class) {
                result = objectPersisters.get(cls);
                if (result == null) {
                    result = new ObjectPersister<T>(cls);

                    objectPersisters.put(cls, result);
                }
            }
        }

        return result;
    }

    public static boolean transactionActive() {
        return transactions.get() != null;
    }

    public static void dispose() {
        ConnectionPool.get().clear();
        MetaDataHandler.get().clear();
        objectPersisters.clear();
    }

    public static void execute(String sql, Object ... params) {
        PreparedStatement statement = null;

            try {
                statement = Persister.getConnection().prepareStatement(sql);
                int index = 1;

                for (Object param : params) {
                    StatementHelper.setStatementParameter(statement, index++, param);
                }

                statement.execute();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static ResultSet executeQuery(String sql, Object ... params) {
        PreparedStatement statement = null;

        try {
            statement = Persister.getConnection().prepareStatement(sql);
            int index = 1;

            for (Object param : params) {
                StatementHelper.setStatementParameter(statement, index++, param);
            }

            return statement.executeQuery();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static int executeUpdate(String sql, Object ... params) {
        PreparedStatement statement = null;

        try {
            statement = Persister.getConnection().prepareStatement(sql);
            int index = 1;

            for (Object param : params) {
                StatementHelper.setStatementParameter(statement, index++, param);
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static void invalidateCache(Class<?> cls) {
        Cache.get().clear(cls);
    }

}
