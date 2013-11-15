package nl.astraeus.database;

import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Date: 11/15/13
 * Time: 9:44 PM
 */
public class ConnectionPool {

    private static ConnectionPool instance = new ConnectionPool();

    public static ConnectionPool get() {
        return instance;
    }

    public ConnectionPool() {
        String [] args = new String[0];

        try {
            Server server = Server.createTcpServer(args).start();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public Connection getConnection() {
        try {
            Class.forName("org.h2.Driver");

            return DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
