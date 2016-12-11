package nl.astraeus.database.jdbc;

import nl.astraeus.database.DdlMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: 11/15/13
 * Time: 9:44 PM
 */
public class ConnectionPool extends ConnectionProvider {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private ConnectionProvider connectionProvider;

    private List<ConnectionWrapper> connectionPool = new LinkedList<>();
    private List<ConnectionWrapper> usedPool = new LinkedList<>();

    private int minimumNumberOfConnections = 0;
    private int maximumNumberOfConnections = 20;

    public ConnectionPool(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;

        for (int index = 0; index < minimumNumberOfConnections; index++) {
            ConnectionWrapper wrapper = new ConnectionWrapper(this, connectionProvider.getConnection());
            connectionPool.add(wrapper);
        }
    }

    @Override
    public DdlMapping.DatabaseDefinition getDefinition() {
        return connectionProvider.getDefinition();
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

    synchronized void returnConnection(ConnectionWrapper connection) {
        if (usedPool.contains(connection)) {
            usedPool.remove(connection);
            connectionPool.add(connection);
            notify();
        } else {
            // discard (clear was probably called)
            connection.dispose();
        }
    }

    public synchronized void dispose() {
        if (!usedPool.isEmpty()) {
            logger.warn("There are still connections being used while ConnectionPool is being disposed!");
        }

        for (ConnectionWrapper wrapper : connectionPool) {
            wrapper.dispose();
        }

        connectionPool.clear();
        usedPool.clear();
    }

    public ConnectionProvider getProvider() {
        return connectionProvider;
    }
}
