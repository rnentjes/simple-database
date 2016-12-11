package nl.astraeus.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.jdbc.ConnectionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: rnentjes
 * Date: 10-12-16
 * Time: 14:50
 */
public class SimpleDatabase {
    private final static Logger logger = LoggerFactory.getLogger(SimpleDatabase.class);

    protected static Map<String, SimpleDatabase> databases = new ConcurrentHashMap<>();

    public static SimpleDatabase get() {
        return get("default");
    }

    public static SimpleDatabase get(String name) {
        SimpleDatabase result = databases.get(name);

        if (result == null) {
            throw new IllegalStateException("Database with name '"+name+"' is not defined!");
        }

        return result;
    }

    public static SimpleDatabase define(ConnectionProvider provider) {
        return define("default", provider);
    }

    public static SimpleDatabase define(String name, ConnectionProvider provider) {
        SimpleDatabase result = databases.get(name);

        if (result != null) {
            throw new IllegalStateException("Database with name '"+name+"' is already defined!");
        }

        SimpleDatabase database = new SimpleDatabase(name, provider);

        databases.put(name, database);

        return database;
    }

    private ThreadLocal<Transaction> transactions = new ThreadLocal<>();
    private Map<Class<?>, ObjectPersister> objectPersisters = new HashMap<>();
    private ConnectionProvider connectionProvider;
    private Map<Class<?>, MetaData> metaData = new ConcurrentHashMap<>();

    private DdlMapping ddlMapping;
    private Cache cache;
    private final String defineName;

    private boolean executeDDLUpdates;

    private SimpleDatabase(String defineName, ConnectionProvider connectionProvider) {
        this.defineName = defineName;
        this.connectionProvider = connectionProvider;
        this.executeDDLUpdates = false;
        this.ddlMapping = new DdlMapping(connectionProvider.getDefinition());
        this.cache = new Cache();
    }

    public void setExecuteDDLUpdates(boolean executeDDLUpdates) {
        this.executeDDLUpdates = executeDDLUpdates;
    }

    public boolean isExecuteDdlUpdates() {
        return executeDDLUpdates;
    }

    public DdlMapping getDdlMapping() {
        return ddlMapping;
    }

    <T> MetaData<T> getMetaData(Class<T> cls) {
        MetaData result = metaData.get(cls);

        if (result == null) {
            synchronized (SimpleDatabase.class) {
                result = metaData.get(cls);

                if (result == null) {
                    long start = System.nanoTime();

                    result = new MetaData<>(cls, this);

                    logger.info("Getting metadata for class {} took {}ms", cls.getSimpleName(), (System.nanoTime() - start) / 1000000f);

                    metaData.put(cls, result);
                }
            }
        }

        return result;
    }

    protected <T> ObjectPersister<T> getObjectPersister(Class<T> cls) {
        ObjectPersister<T> result = objectPersisters.get(cls);

        if (result == null) {
            result = new ObjectPersister<>(cls, getMetaData(cls), cache);

            objectPersisters.put(cls, result);
        }

        return result;
    }

    public Connection getNewConnection() {
        try {
            return connectionProvider.getConnection();
        } catch(SQLException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Connection getConnection() {
        if (transactions.get() == null) {
            throw new IllegalStateException("No transaction active!");
        }

        return transactions.get().getConnection();
    }

    public boolean transactionActive() {
        return transactions.get() != null;
    }

    public void begin() {
        transactions.set(new Transaction(getNewConnection()));
    }

    public void commit() {
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

    public void rollback() {
        if (transactions.get() != null) {
            Connection connection = transactions.get().getConnection();
            try {
                connection.rollback();
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

    public void execute(String sql, Object ... params) {
        PreparedStatement statement = null;

        try {
            statement = getConnection().prepareStatement(sql);
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
                if (System.err != null) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    public ResultSet executeQuery(String sql, Object ... params) {
        PreparedStatement statement = null;

        try {
            statement = getConnection().prepareStatement(sql);
            int index = 1;

            for (Object param : params) {
                StatementHelper.setStatementParameter(statement, index++, param);
            }

            return statement.executeQuery();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public int executeUpdate(String sql, Object ... params) {
        PreparedStatement statement = null;

        try {
            statement = getConnection().prepareStatement(sql);
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
                if (System.err != null) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    public Cache getCache() {
        return cache;
    }

    public void invalidateCache(Class<?> cls) {
        cache.clear(cls);
    }

    public void dispose() {
        connectionProvider.dispose();
        metaData.clear();
        objectPersisters.clear();
        databases.remove(defineName);
    }
}
