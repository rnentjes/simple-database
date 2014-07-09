package nl.astraeus.database.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: 11/15/13
 * Time: 9:44 PM
 */
public class ConnectionPool {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private static ConnectionPool instance = new ConnectionPool();

    public static ConnectionPool get() {
        return instance;
    }

    private ConnectionProvider connectionProvider = null;

    private List<ConnectionWrapper> connectionPool = new LinkedList<>();
    private List<ConnectionWrapper> usedPool = new LinkedList<>();

    private int minimumNumberOfConnections = 0;
    private int maximumNumberOfConnections = 20;

    public ConnectionPool() {
    }

    public synchronized void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;

        for (ConnectionWrapper conn : connectionPool) {
            conn.dispose();
        }

        connectionPool.clear();

        for (int index = 0; index < minimumNumberOfConnections; index++) {
            ConnectionWrapper wrapper = new ConnectionWrapper(this, connectionProvider.getConnection());
            connectionPool.add(wrapper);
        }
    }

    public synchronized Connection getConnection() {
        if (connectionPool.isEmpty() && usedPool.size() < maximumNumberOfConnections) {
            ConnectionWrapper wrapper = new ConnectionWrapper(this, connectionProvider.getConnection());

            usedPool.add(wrapper);
            return wrapper;
        }

        while(connectionPool.isEmpty()) {
            // wait for it
            try {
                wait();
            } catch (InterruptedException e) {
                logger.info(e.getMessage(), e);
            }
        }

        ConnectionWrapper result = connectionPool.remove(0);
        usedPool.add(result);
        return result;
    }

    protected synchronized void returnConnection(ConnectionWrapper connection) {
        if (usedPool.contains(connection)) {
            usedPool.remove(connection);
            connectionPool.add(connection);
            notify();
        } else {
            // discard (clear was probably called)
            connection.dispose();
        }
    }

    public boolean hasConnectionProvider() {
        return connectionProvider != null;
    }

    public synchronized void clear() {
        connectionPool.clear();
        usedPool.clear();
    }

    public ConnectionProvider getProvider() {
        return connectionProvider;
    }
}
