package nl.astraeus.database;

import nl.astraeus.database.annotations.*;
import nl.astraeus.template.SimpleTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 8:59 PM
 */
public class FieldMetaData {
    private final static Logger logger = LoggerFactory.getLogger(FieldMetaData.class);

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

    private static Map<Class<?>, Integer> sqlTypeMapping;

//    java.lang.Boolean BOOLEAN
//    java.lang.Byte TINYINT
//    java.lang.BigDecinal DECIMAL(${precision}, ${scale})

    private static Map<String, DdlMapping> ddlMappingx;

    // todo: get these definitions from some configuration file
    static {
        sqlTypeMapping = new HashMap<>();

        sqlTypeMapping.put(String.class, Types.VARCHAR);
        sqlTypeMapping.put(Long.class, Types.BIGINT);
        sqlTypeMapping.put(long.class, Types.BIGINT);
        sqlTypeMapping.put(Integer.class, Types.INTEGER);
        sqlTypeMapping.put(int.class, Types.INTEGER);
        sqlTypeMapping.put(Short.class, Types.SMALLINT);
        sqlTypeMapping.put(short.class, Types.SMALLINT);
        sqlTypeMapping.put(Double.class, Types.DECIMAL);
        sqlTypeMapping.put(double.class, Types.DECIMAL);
        sqlTypeMapping.put(Boolean.class, Types.BOOLEAN);
        sqlTypeMapping.put(boolean.class, Types.BOOLEAN);
        sqlTypeMapping.put(BigDecimal.class, Types.DECIMAL);
        sqlTypeMapping.put(java.util.Date.class, Types.TIMESTAMP);
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

        SimpleTemplate template = DdlMapping.get().getDdlTemplateForType(javaType);
        sqlType = sqlTypeMapping.get(javaType);

        String type = "BIGINT"; // default to id ref

        Serialized serialized = field.getAnnotation(Serialized.class);
        Collection collection = field.getAnnotation(Collection.class);

        if (serialized != null) {
            // BLOB
            type = DdlMapping.get().getBlobType().render(model);
            sqlType = Types.BLOB;
            this.type = ColumnType.SERIALIZED;
        } else if (collection != null) {
            collectionClass = collection.value();
            this.type = ColumnType.COLLECTION;

            // BLOB
            type = DdlMapping.get().getBlobType().render(model);
            sqlType = Types.BLOB;
        } else if (javaType.getAnnotation(Table.class) != null) {
            // oneToone
            type = DdlMapping.get().getIdType().render(model);
            sqlType = Types.BIGINT;
            this.type = ColumnType.REFERENCE;
        } else if (template != null) {
            type = template.render(model);
        } else {
            throw new IllegalStateException("Type "+field.getType().getSimpleName()+" of field "+field.getDeclaringClass().getSimpleName()+"."+field.getName()+" is not supported!");
        }

        columnInfo = new ColumnInfo(columnName, type);
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public ColumnType getType() {
        return type;
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
        Long id = null;

        if (value == null) {
            switch(type) {
                case BASIC:
                    statement.setNull(index, sqlType);
                    break;
                case REFERENCE:
                    statement.setNull(index, Types.BIGINT);
                    break;
                case COLLECTION:
                case SERIALIZED:
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
                        case Types.BOOLEAN:
                            statement.setBoolean(index, (Boolean) value);
                            break;
                        case Types.DECIMAL:
                            if (javaType.equals(BigDecimal.class)) {
                                statement.setBigDecimal(index, (BigDecimal) value);
                            } else {
                                statement.setDouble(index, (Double) value);
                            }
                            break;
                        case Types.TIMESTAMP:
                            statement.setTimestamp(index, new Timestamp(((java.util.Date)value).getTime()));
                            break;
                    }
                    break;
                case REFERENCE:
                    metaData = MetaDataHandler.get().getMetaData(value.getClass());

                    id = metaData.getId(value);

                    if (id == null || id == 0) {
                        Persister.insert(value);

                        id = metaData.getId(value);
                    }

                    statement.setLong(index, id);
                    break;
                case COLLECTION:
                    metaData = MetaDataHandler.get().getMetaData(collectionClass);
                    java.util.Collection c = (java.util.Collection)value;
                    ByteBuffer buffer = ByteBuffer.allocate(c.size() * 8);
                    for (Object o : c) {
                        id = metaData.getId(o);

                        if (id == null || id == 0) {
                            Persister.insert(o);

                            id = metaData.getId(o);
                        }

                        buffer.putLong(id);
                    }

                    statement.setBlob(index, new ByteArrayInputStream(buffer.array()));
                    break;
                case SERIALIZED:
                    try (ByteArrayOutputStream objectStreamBuffer = new ByteArrayOutputStream();
                         ObjectOutputStream out = new ObjectOutputStream(objectStreamBuffer)) {

                        out.writeObject(value);

                        statement.setBlob(index, new ByteArrayInputStream(objectStreamBuffer.toByteArray()));
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    break;
            }
        }
    }

    public void set(ResultSet rs, int index, Object obj) throws SQLException {
        Long id;

        if (obj == null) {
            set(obj, null);
        } else {
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
                        case Types.BOOLEAN:
                            set(obj, rs.getBoolean(index));
                            break;
                        case Types.DECIMAL:
                            if (javaType.equals(BigDecimal.class)) {
                                set(obj, rs.getBigDecimal(index));
                            } else {
                                set(obj, rs.getDouble(index));
                            }
                            break;
                        case Types.TIMESTAMP:
                            Timestamp stamp = rs.getTimestamp(index);

                            if (stamp != null) {
                                set(obj, new java.util.Date(rs.getTimestamp(index).getTime()));
                            } else {
                                set(obj, null);
                            }
                            break;
                    }
                    break;
                case REFERENCE:
                    id = rs.getLong(index);

                    if (id > 0L) {
                        Object object = Persister.find(javaType, id);

                        if (object == null) {
                            logger.warn("Missing reference detected "+field.getDeclaringClass().getSimpleName()+"."+getFieldName()+":"+id);
                        }

                        set(obj, object);
                    } else {
                        set(obj, null);
                    }
                    break;
                case COLLECTION:
                    Blob blob = rs.getBlob(index);
                    MetaData meta = MetaDataHandler.get().getMetaData(collectionClass);
                    ReferentList list = new ReferentList(collectionClass, meta);

                    ByteBuffer buffer = ByteBuffer.wrap(blob.getBytes(0, (int) blob.length()));

                    while(buffer.hasRemaining()) {
                        id = buffer.getLong();

                        list.addId(id);
                    }

                    set(obj, list);
                    break;
                case SERIALIZED:
                    Blob blub = rs.getBlob(index);
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(blub.getBytes(0, (int) blub.length()));
                        ObjectInputStream ois = new ObjectInputStream(bais)) {

                        set(obj, ois.readObject());
                    } catch (ClassNotFoundException | IOException e) {
                        throw new IllegalStateException(e);
                    }
                    break;
            }
        }

    }

    public <T> void reloadReference(T result) {
        Object current = get(result);

        if (current != null) {
            MetaData meta = MetaDataHandler.get().getMetaData(current.getClass());

            Long id = meta.getId(current);

            if (id != null && id > 0) {
                Object object = Persister.find(javaType, id);

                if (object == null) {
                    logger.warn("Missing reference detected "+field.getDeclaringClass().getSimpleName()+"."+getFieldName()+":"+id);
                }

                set(result, object);
            } else {
                set(result, null);
            }
        }
    }

}
