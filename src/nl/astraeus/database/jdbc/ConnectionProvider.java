package nl.astraeus.database.jdbc;

import java.sql.Connection;

import nl.astraeus.database.DdlMapping;

/**
 * Date: 11/16/13
 * Time: 12:00 PM
 */
public abstract class ConnectionProvider {

    public void dispose() {}

    abstract public Connection getConnection();

    public DdlMapping.DatabaseDefinition getDefinition() {
        return DdlMapping.DatabaseDefinition.H2;
    }

}
