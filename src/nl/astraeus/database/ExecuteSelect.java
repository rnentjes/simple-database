package nl.astraeus.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: rnentjes
 * Date: 10-12-16
 * Time: 12:57
 */
public class ExecuteSelect<T> extends ExecuteConnectionWithResult<List<T>> {

    private final MetaData<T> metaData;
    private final String query;
    private final Object[] params;

    public ExecuteSelect(MetaData<T> metaData, String query, Object[] params) {
        this.metaData = metaData;
        this.query = query;
        this.params = params;
    }

    @Override
    public List<T> execute(Connection connection) throws SQLException {
        List<T> result = new ArrayList<>();
        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(query);
            int index = 1;

            for (Object param : params) {
                StatementHelper.setStatementParameter(statement, index++, param);
            }

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Long id = rs.getLong(1);

                result.add((T) metaData.find(id));
            }

            return result;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
}
