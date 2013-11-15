package nl.astraeus.database;

import nl.astraeus.database.annotations.*;
import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 8:59 PM
 */
public class FieldMetaData {

    private Field field;
    private String fieldName;

    private Class<?> javaType;
    private Integer sqlType;

    private ColumnInfo  columnInfo;

    private Length length;
    private Default defaultValue;

    private static Map<Class<?>, SimpleTemplate> ddlMapping;
    private static Map<Class<?>, Integer> sqlTypeMapping;

//    java.lang.Boolean BOOLEAN
//    java.lang.Byte TINYINT
//    java.lang.BigDecinal DECIMAL(${precision}, ${scale})

    // todo: get these definitions from some configuration file
    static {
        ddlMapping = new HashMap<>();
        sqlTypeMapping = new HashMap<>();

        ddlMapping.put(String.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "VARCHAR(${length})"));
        ddlMapping.put(Long.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "BIGINT"));
        ddlMapping.put(long.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "BIGINT"));
        ddlMapping.put(Integer.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "INT"));
        ddlMapping.put(int.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "INT"));
        ddlMapping.put(Short.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "SMALLINT"));
        ddlMapping.put(short.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "SMALLINT"));
        ddlMapping.put(Double.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "DECIMAL(${precision}, ${scale})"));
        ddlMapping.put(double.class, new SimpleTemplate("${", "}", EscapeMode.NONE, "DECIMAL(${precision}, ${scale})"));

        sqlTypeMapping.put(String.class, Types.VARCHAR);
        sqlTypeMapping.put(Long.class, Types.BIGINT);
        sqlTypeMapping.put(long.class, Types.BIGINT);
        sqlTypeMapping.put(Integer.class, Types.INTEGER);
        sqlTypeMapping.put(int.class, Types.INTEGER);
        sqlTypeMapping.put(Short.class, Types.SMALLINT);
        sqlTypeMapping.put(short.class, Types.SMALLINT);
        sqlTypeMapping.put(Double.class, Types.DECIMAL);
        sqlTypeMapping.put(double.class, Types.DECIMAL);
    }

    public FieldMetaData(Field field) {
        this.field = field;
        this.field.setAccessible(true);

        fieldName = field.getName();
        String columnName = fieldName;
        javaType = field.getType();

        length = field.getAnnotation(Length.class);
        defaultValue = field.getAnnotation(Default.class);
        Column column = field.getAnnotation(Column.class);

        if (column != null) {
            columnName = column.name();
        }

        Map<String, Object> model = new HashMap<>();

        if (length == null) {
            model.put("length", 255);
            model.put("precision", 10);
            model.put("scale", 2);
        } else {
            model.put("length", length.value());
            model.put("precision", length.precision());
            model.put("scale", length.scale());
        }

        SimpleTemplate template = ddlMapping.get(javaType);
        sqlType = sqlTypeMapping.get(javaType);

        String type = "BIGINT"; // default to id ref

        if (template != null) {
            type = template.render(model);
        } else {
            Collection collection = field.getAnnotation(Collection.class);

            if (collection != null) {
                Class<?> collectionClass = collection.value();

                // BLOB
                type = "BLOB";
                sqlType = Types.BLOB;
            } else {
                Serialized serialized = field.getAnnotation(Serialized.class);

                if (serialized != null) {
                    // BLOB
                    type = "BLOB";
                    sqlType = Types.BLOB;
                } else if (javaType.getAnnotation(Table.class) != null) {
                    // oneToone
                    type = "BIGINT";
                    sqlType = Types.BIGINT;
                } else {
                    throw new IllegalStateException("Type "+field.getType().getSimpleName()+" of field "+field.getDeclaringClass().getSimpleName()+"."+field.getName()+" is not supported!");
                }
            }
        }

        columnInfo = new ColumnInfo(columnName, type);
    }

    public Field getField() {
        return field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Class getJavaType() {
        return javaType;
    }

    public ColumnInfo getColumnInfo() {
        return columnInfo;
    }

    public Length getLength() {
        return length;
    }

    public Default getDefaultValue() {
        return defaultValue;
    }

    public Object get(Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public void set(Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public void set(PreparedStatement statement, int index, Object obj) throws SQLException {
        Object value = get(obj);

        switch(sqlType) {
            case Types.VARCHAR:
                statement.setString(index, (String)value);
                break;
            case Types.BIGINT:
                statement.setLong(index, (Long) value);
                break;
            case Types.INTEGER:
                statement.setInt(index, (Integer) value);
                break;
            case Types.SMALLINT:
                statement.setShort(index, (Short) value);
                break;
            case Types.DECIMAL:
                statement.setDouble(index, (Double) value);
                break;
            case Types.BLOB:
                //statement.setBlob(index, (Double) value);
                break;
        }
    }

    public void set(ResultSet rs, int index, Object obj) throws SQLException {
        switch(sqlType) {
            case Types.VARCHAR:
                set(obj, rs.getString(index));
                break;
            case Types.BIGINT:
                set(obj, rs.getLong(index));
                break;
            case Types.INTEGER:
                set(obj, rs.getInt(index));
                break;
            case Types.SMALLINT:
                set(obj, rs.getShort(index));
                break;
            case Types.DECIMAL:
                set(obj, rs.getDouble(index));
                break;
            case Types.BLOB:
                //set(obj, rs.getString(index));
                //statement.setBlob(index, (Double) value);
                break;
        }

    }
}
