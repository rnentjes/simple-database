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
    private int minimumNumberOfConnections = 0;
    private int maximumNumberOfConnections = 10;
    private int used = 0;

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
        if (connectionPool.isEmpty() && used < maximumNumberOfConnections) {
            ConnectionWrapper wrapper = new ConnectionWrapper(this, connectionProvider.getConnection());

            used++;
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

        used++;
        return connectionPool.remove(0);
    }

    protected synchronized void returnConnection(ConnectionWrapper connection) {
        used--;
        connectionPool.add(connection);
        notify();
    }

    public boolean hasConnectionProvider() {
        return connectionProvider != null;
    }

    public synchronized void clear() {
        connectionPool = new LinkedList<>();
    }
}
