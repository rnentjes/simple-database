package nl.astraeus.database;

import nl.astraeus.database.annotations.*;
import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 8:59 PM
 */
public class FieldMetaData {

    public static enum ColumnType {
        BASIC,
        COLLECTION,
        SERIALIZED,
        REFERENCE;
    }

    private Field field;
    private String fieldName;

    private ColumnType type;
    private Class<?> javaType;
    private Integer sqlType;

    private ColumnInfo  columnInfo;

    private Length length;
    private Default defaultValue;
    private Class<?> collectionClass;

    private boolean primaryKey = false;

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
        type = ColumnType.BASIC;

        length = field.getAnnotation(Length.class);
        defaultValue = field.getAnnotation(Default.class);
        Column column = field.getAnnotation(Column.class);
        Id id = field.getAnnotation(Id.class);

        if (column != null) {
            columnName = column.name();
        }

        if (id != null) {
            primaryKey = true;
            field.setAccessible(true);
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
                collectionClass = collection.value();
                this.type = ColumnType.COLLECTION;

                // BLOB
                type = "BLOB";
                sqlType = Types.BLOB;
            } else {
                Serialized serialized = field.getAnnotation(Serialized.class);

                if (serialized != null) {
                    // BLOB
                    type = "BLOB";
                    sqlType = Types.BLOB;
                    this.type = ColumnType.SERIALIZED;
                } else if (javaType.getAnnotation(Table.class) != null) {
                    // oneToone
                    type = "BIGINT";
                    sqlType = Types.BIGINT;
                    this.type = ColumnType.REFERENCE;
                } else {
                    throw new IllegalStateException("Type "+field.getType().getSimpleName()+" of field "+field.getDeclaringClass().getSimpleName()+"."+field.getName()+" is not supported!");
                }
            }
        }

        columnInfo = new ColumnInfo(columnName, type);
    }

    public boolean isPrimaryKey() {
        return primaryKey;
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
        MetaData metaData = null;

        if (value == null) {
            switch(type) {
                case BASIC:
                    statement.setNull(index, sqlType);
                    break;
                case REFERENCE:
                    statement.setNull(index, Types.BIGINT);
                    break;
                case COLLECTION:
                    statement.setNull(index, Types.BLOB);
                    break;

            }
        } else {
            switch(type) {
                case BASIC:
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
                    }
                    break;
                case REFERENCE:
                    metaData = MetaDataHandler.get().getMetaData(value.getClass());
                    statement.setLong(index, metaData.getId(value));
                    break;
                case COLLECTION:
                    metaData = MetaDataHandler.get().getMetaData(collectionClass);
                    java.util.Collection c = (java.util.Collection)value;
                    ByteBuffer buffer = ByteBuffer.allocate(c.size() * 8);
                    for (Object o : c) {
                        buffer.putLong(metaData.getId(o));
                    }

                    statement.setBlob(index, new ByteArrayInputStream(buffer.array()));
                    break;
            }
        }
    }

    public void set(ResultSet rs, int index, Object obj) throws SQLException {
        Long id = null;

        switch(type) {
            case BASIC:
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
                }
                break;
            case REFERENCE:
                id = rs.getLong(index);

                set(obj, Persister.find(javaType, id));
                break;
            case COLLECTION:
                Blob blob = rs.getBlob(index);

                ByteBuffer buffer = ByteBuffer.wrap(blob.getBytes(0, (int) blob.length()));

                while(buffer.hasRemaining()) {
                    id = buffer.getLong();

                    // todo: do something with these ids
                }
                break;
        }

    }
}
