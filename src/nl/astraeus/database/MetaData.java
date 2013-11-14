package nl.astraeus.database;

import nl.astraeus.database.annotations.Table;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData {


    private Class<?> cls;
    private String tableName;
    private Map<Field, FieldMetaData> fields;

    public MetaData(Connection connection, Class<?> cls) {
        this.cls = cls;

        processAnnotation(cls.getAnnotation(Table.class));

        if (tableName == null) {
            tableName = cls.getSimpleName();
        }

        this.fields = new HashMap<>();

        for (Field field : cls.getDeclaredFields()) {
            FieldMetaData info = new FieldMetaData(field);

            fields.put(field, info);
        }

        try {
            // get metadata from database
            ResultSet result = connection.getMetaData().getTables(null, null, tableName, null);

            if (result.next()) {

            } else {
                createTable(connection);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    private void processAnnotation(Table table) {
        if (table != null) {
            tableName = table.name();
        }
    }

    private void createTable(Connection connection) {
        Map<String, Object> model = new HashMap<>();

        List<ColumnInfo> columns = new ArrayList<>();

        model.put("tableName", tableName);
        model.put("columns", columns);

    }

}
