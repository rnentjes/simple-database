package nl.astraeus.database.jdbc;

import nl.astraeus.database.ConnectionProvider;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
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

    private ConnectionProvider connectionProvider;

    private List<ConnectionWrapper> connectionPool = new LinkedList<>();
    private int minimumNumberOfConnections = 2;
    private int maximumNumberOfConnections = 10;
    private int used = 0;

    public ConnectionPool() {
        String [] args = new String[0];

        try {
            Server server = Server.createTcpServer(args).start();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
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
            return new ConnectionWrapper(this, connectionProvider.getConnection());
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

}
