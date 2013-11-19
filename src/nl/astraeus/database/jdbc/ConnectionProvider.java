package nl.astraeus.database.jdbc;

import java.sql.Connection;

/**
 * Date: 11/16/13
 * Time: 12:00 PM
 */
public interface ConnectionProvider {

    public Connection getConnection();

}
