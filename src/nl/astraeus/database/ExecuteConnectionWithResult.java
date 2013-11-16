package nl.astraeus.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * User: rnentjes
 * Date: 11/16/13
 * Time: 3:57 PM
 */
public abstract class ExecuteConnectionWithResult<T> {

    public abstract T execute(Connection connection) throws SQLException;

}
