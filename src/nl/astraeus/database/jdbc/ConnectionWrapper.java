package nl.astraeus.database.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Date: 11/16/13
 * Time: 12:16 PM
 */
public class ConnectionWrapper implements Connection {
    private final static Logger logger = LoggerFactory.getLogger(ConnectionWrapper.class);

    private ConnectionPool pool;
    private Connection connection;

    public ConnectionWrapper(ConnectionPool pool, Connection connection) {
        this.pool = pool;
        this.connection = connection;
    }

    public void dispose() {
        try {
            connection.close();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String s) throws SQLException {
        return connection.prepareStatement(s);
    }

    @Override
    public CallableStatement prepareCall(String s) throws SQLException {
        return connection.prepareCall(s);
    }

    @Override
    public String nativeSQL(String s) throws SQLException {
        return connection.nativeSQL(s);
    }

    @Override
    public void setAutoCommit(boolean b) throws SQLException {
        connection.setAutoCommit(b);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        connection.rollback();

        pool.returnConnection(this);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }

    @Override
    public void setReadOnly(boolean b) throws SQLException {
        connection.setReadOnly(b);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }

    @Override
    public void setCatalog(String s) throws SQLException {
        connection.setCatalog(s);
    }

    @Override
    public String getCatalog() throws SQLException {
        return connection.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int i) throws SQLException {
        connection.setTransactionIsolation(i);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return connection.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return connection.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        connection.clearWarnings();
    }

    @Override
    public Statement createStatement(int i, int i2) throws SQLException {
        return connection.createStatement(i, i2);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i2) throws SQLException {
        return connection.prepareStatement(s, i, i2);
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i2) throws SQLException {
        return prepareCall(s, i, i2);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return connection.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> stringClassMap) throws SQLException {
        connection.setTypeMap(stringClassMap);
    }

    @Override
    public void setHoldability(int i) throws SQLException {
        connection.setHoldability(i);
    }

    @Override
    public int getHoldability() throws SQLException {
        return connection.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return connection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String s) throws SQLException {
        return connection.setSavepoint(s);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        connection.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        connection.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int i, int i2, int i3) throws SQLException {
        return connection.createStatement(i, i2, i3);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i2, int i3) throws SQLException {
        return connection.prepareStatement(s, i, i2, i3);
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i2, int i3) throws SQLException {
        return connection.prepareCall(s, i, i2, i3);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i) throws SQLException {
        return connection.prepareStatement(s, i);
    }

    @Override
    public PreparedStatement prepareStatement(String s, int[] ints) throws SQLException {
        return connection.prepareStatement(s, ints);
    }

    @Override
    public PreparedStatement prepareStatement(String s, String[] strings) throws SQLException {
        return connection.prepareStatement(s, strings);
    }

    @Override
    public Clob createClob() throws SQLException {
        return connection.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return connection.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return connection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return connection.createSQLXML();
    }

    @Override
    public boolean isValid(int i) throws SQLException {
        return connection.isValid(i);
    }

    @Override
    public void setClientInfo(String s, String s2) throws SQLClientInfoException {
        connection.setClientInfo(s, s2);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String s) throws SQLException {
        return connection.getClientInfo(s);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return connection.getClientInfo();
    }

    @Override
    public Array createArrayOf(String s, Object[] objects) throws SQLException {
        return connection.createArrayOf(s, objects);
    }

    @Override
    public Struct createStruct(String s, Object[] objects) throws SQLException {
        return connection.createStruct(s, objects);
    }

    @Override
    public void setSchema(String s) throws SQLException {
        connection.setSchema(s);
    }

    @Override
    public String getSchema() throws SQLException {
        return connection.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        connection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int i) throws SQLException {
        connection.setNetworkTimeout(executor, i);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return connection.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return connection.unwrap(tClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return connection.isWrapperFor(aClass);
    }
}
