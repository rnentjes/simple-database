package nl.astraeus.database;

import java.sql.Connection;

/**
 * Date: 11/13/13
 * Time: 9:32 PM
 */
public class Transaction {

    private Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }


}

