package nl.astraeus.database;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * User: rnentjes
 * Date: 2/15/14
 * Time: 11:16 AM
 */
public class StatementHelper {

    protected static void setStatementParameter(PreparedStatement statement, int index, Object param) throws SQLException {
        if (param == null) {
            throw new IllegalArgumentException("Parameters can not be null!");
        } else {
            if (param.getClass().equals(String.class)) {
                statement.setString(index, (String) param);
            } else if (param.getClass().equals(Long.class) || param.getClass().equals(long.class)) {
                statement.setLong(index, (Long) param);
            } else if (param.getClass().equals(Integer.class) || param.getClass().equals(int.class)) {
                statement.setInt(index, (Integer) param);
            } else if (param.getClass().equals(Short.class) || param.getClass().equals(short.class)) {
                statement.setShort(index, (Short) param);
            } else if (param.getClass().equals(Double.class) || param.getClass().equals(double.class)) {
                statement.setDouble(index, (Double) param);
            } else if (param.getClass().equals(BigDecimal.class)) {
                statement.setBigDecimal(index, (BigDecimal) param);
            } else if (param.getClass().equals(Boolean.class) || param.getClass().equals(boolean.class)) {
                statement.setBoolean(index, (Boolean) param);
            } else if (param.getClass().equals(Date.class)) {
                Timestamp date = new java.sql.Timestamp(((Date) param).getTime());

                statement.setTimestamp(index, date);
            } else {
                throw new IllegalStateException("Type " + param.getClass() + " not supported in where queries yet!");
            }
        }
    }

}
