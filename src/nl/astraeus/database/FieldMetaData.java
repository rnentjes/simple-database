package nl.astraeus.database;

import nl.astraeus.database.annotations.Blob;
import nl.astraeus.database.annotations.Clob;
import nl.astraeus.database.annotations.Collection;
import nl.astraeus.database.annotations.Column;
import nl.astraeus.database.annotations.Default;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Index;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Reference;
import nl.astraeus.database.annotations.Serialized;
import nl.astraeus.database.annotations.Table;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 8:59 PM
 */
public class FieldMetaData {
    private final static Logger logger = LoggerFactory.getLogger(FieldMetaData.class);

    public enum ColumnType {
        BASIC,
        COLLECTION,
        SERIALIZED,
        REFERENCE,
        BLOB,
        CLOB
    }

    private SimpleDatabase db;
    private DdlMapping ddlMapping;
    private Field field;
    private String fieldName;

    private ColumnType type;
    private Class<?> javaType;
    private Integer sqlType;

    private ColumnInfo  columnInfo;

    private Reference reference;
    private Length length;
    private Default defaultValue;
    private Index index;
    private Class<?> collectionClass;

    private boolean primaryKey = false;

    private static Map<Class<?>, Integer> sqlTypeMapping;

    static {
        sqlTypeMapping = new HashMap<>();

        sqlTypeMapping.put(String.class, Types.VARCHAR);
        sqlTypeMapping.put(Long.class, Types.BIGINT);
        sqlTypeMapping.put(long.class, Types.BIGINT);
        sqlTypeMapping.put(Integer.class, Types.INTEGER);
        sqlTypeMapping.put(int.class, Types.INTEGER);
        sqlTypeMapping.put(Short.class, Types.SMALLINT);
        sqlTypeMapping.put(short.class, Types.SMALLINT);
        sqlTypeMapping.put(Float.class, Types.DECIMAL);
        sqlTypeMapping.put(float.class, Types.DECIMAL);
        sqlTypeMapping.put(Double.class, Types.DECIMAL);
        sqlTypeMapping.put(double.class, Types.DECIMAL);
        sqlTypeMapping.put(Boolean.class, Types.BOOLEAN);
        sqlTypeMapping.put(boolean.class, Types.BOOLEAN);
        sqlTypeMapping.put(BigDecimal.class, Types.DECIMAL);
        sqlTypeMapping.put(java.util.Date.class, Types.TIMESTAMP);
    }

    public FieldMetaData(SimpleDatabase db, Field field) {
        this.db = db;
        this.ddlMapping = db.getDdlMapping();
        this.field = field;
        this.field.setAccessible(true);

        fieldName = field.getName();
        String columnName = fieldName;
        javaType = field.getType();
        type = ColumnType.BASIC;

        reference = field.getAnnotation(Reference.class);
        length = field.getAnnotation(Length.class);
        defaultValue = field.getAnnotation(Default.class);
        index = field.getAnnotation(Index.class);
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

        SimpleTemplate template = ddlMapping.getDdlTemplateForType(javaType);
        sqlType = sqlTypeMapping.get(javaType);

        String type;

        Blob blob = field.getAnnotation(Blob.class);
        Clob clob = field.getAnnotation(Clob.class);
        Serialized serialized = field.getAnnotation(Serialized.class);
        Collection collection = field.getAnnotation(Collection.class);

        if (blob != null) {
            // BLOB
            type = ddlMapping.getBlobType().render(model);
            sqlType = Types.BLOB;
            this.type = ColumnType.BLOB;
        } else if (clob != null) {
            if (!field.getType().equals(String.class)) {
                throw new IllegalStateException("Clob is only allowed on String objects.");
            }
            // CLOB
            type = ddlMapping.getClobType().render(model);
            sqlType = Types.CLOB;
            this.type = ColumnType.CLOB;
        } else if (serialized != null) {
            // BLOB
            type = ddlMapping.getBlobType().render(model);
            sqlType = Types.BLOB;
            this.type = ColumnType.SERIALIZED;
        } else if (collection != null) {
            collectionClass = collection.value();
            this.type = ColumnType.COLLECTION;

            // BLOB
            type = ddlMapping.getBlobType().render(model);
            sqlType = Types.BLOB;
        } else if (javaType.getAnnotation(Table.class) != null) {
            // oneToone
            type = ddlMapping.getIdType().render(model);
            sqlType = Types.BIGINT;
            this.type = ColumnType.REFERENCE;
        } else if (template != null) {
            type = template.render(model);
        } else {
            throw new IllegalStateException("Type "+field.getType().getSimpleName()+" of field "+field.getDeclaringClass().getSimpleName()+"."+field.getName()+" is not supported!");
        }

        if (ddlMapping.ddlNamesInUppercase()) {
            columnName = columnName.toUpperCase();
        }

        columnInfo = new ColumnInfo(columnName, type);
    }

    protected Reference getReference() {
        return reference;
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

    public boolean hasIndex() {
        return index != null;
    }

    public boolean hasUniqueIndex() {
        return index != null && index.unique();
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
                case BLOB:
                case CLOB:
                case COLLECTION:
                case SERIALIZED:
                    statement.setNull(index, Types.BLOB);
                    break;
            }
        } else {
            byte [] bytes;

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
                            } else if (javaType.equals(Float.class) || javaType.equals(float.class)) {
                                statement.setFloat(index, (Float) value);
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
                    metaData = db.getMetaData(value.getClass());

                    id = metaData.getId(value);

                    if (id == null || id == 0) {
                        metaData.insert(value);

                        id = metaData.getId(value);
                    }

                    statement.setLong(index, id);
                    break;
                case COLLECTION:
                    metaData = db.getMetaData(collectionClass);
                    java.util.Collection c = (java.util.Collection)value;
                    ByteBuffer buffer = ByteBuffer.allocate(c.size() * 8);
                    for (Object o : c) {
                        id = metaData.getId(o);

                        if (id == null || id == 0) {
                            metaData.insert(o);

                            id = metaData.getId(o);
                        }

                        buffer.putLong(id);
                    }

                    statement.setBinaryStream(index, new ByteArrayInputStream(buffer.array()), buffer.position());
                    break;
                case SERIALIZED:
                    try (ByteArrayOutputStream objectStreamBuffer = new ByteArrayOutputStream();
                         ObjectOutputStream out = new ObjectOutputStream(objectStreamBuffer)) {

                        out.writeObject(value);

                        bytes = objectStreamBuffer.toByteArray();

                        statement.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    break;
                case BLOB:
                    bytes = (byte[])value;

                    statement.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
                    break;
                case CLOB:
                    try {
                        bytes = ((String)value).getBytes("UTF-8");

                        statement.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
                    } catch (UnsupportedEncodingException e) {
                        throw new IllegalStateException(e);
                    }
                    break;
            }
        }
    }

    public void set(ResultSet rs, int index, Object obj) throws SQLException {
        Long id;

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
                        } else if (javaType.equals(Float.class) || javaType.equals(float.class)) {
                            set(obj, rs.getFloat(index));
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
                    // check for circular references

                    Object object = db.getObjectPersister(javaType).find(id);

                    if (object == null) {
                        logger.warn("Missing reference detected "+field.getDeclaringClass().getSimpleName()+"."+getFieldName()+":"+id);
                    }

                    set(obj, object);
                } else {
                    set(obj, null);
                }
                break;
            case COLLECTION:
                try (InputStream in = rs.getBinaryStream(index)) {
                    MetaData meta = db.getMetaData(collectionClass);
                    ReferentList list = new ReferentList(collectionClass, meta);

                    if (in != null) {
                        ByteBuffer buffer = ByteBuffer.wrap(Util.readInputStream(in));

                        while(buffer.hasRemaining()) {
                            id = buffer.getLong();

                            list.addId(id);
                        }
                    }

                    set(obj, list);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }

                break;
            case SERIALIZED:
                try (InputStream in = rs.getBinaryStream(index)) {
                    if (in != null) {
                        try (ObjectInputStream ois = new ObjectInputStream(in)) {
                                 set(obj, ois.readObject());
                        }
                    } else {
                        set(obj, null);
                    }
                } catch (ClassNotFoundException | IOException e) {
                    throw new IllegalStateException(e);
                }
                break;
            case BLOB:
                try (InputStream in = rs.getBinaryStream(index);
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                    if (in != null) {
                        byte[] buffer = new byte[8196];
                        int nr;
                        while ((nr = in.read(buffer)) > 0) {
                            out.write(buffer, 0, nr);
                        }

                        set(obj, out.toByteArray());
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                break;
            case CLOB:
                try (InputStream in = rs.getBinaryStream(index);
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                    if (in != null) {
                        byte[] buffer = new byte[8196];
                        int nr;
                        while ((nr = in.read(buffer)) > 0) {
                            out.write(buffer, 0, nr);
                        }

                        set(obj, new String(out.toByteArray(), "UTF-8"));
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                break;
        }
    }

    public <T> void reloadReference(T result) {
        Object current = get(result);

        if (current != null) {
            MetaData meta = db.getMetaData(current.getClass());

            Long id = meta.getId(current);

            if (id != null && id > 0) {
                Object object = db.getObjectPersister(javaType).find(id);

                if (object == null) {
                    logger.warn("Missing reference detected "+field.getDeclaringClass().getSimpleName()+"."+getFieldName()+":"+id);
                }

                set(result, object);
            } else {
                set(result, null);
            }
        }
    }

    public Integer getSqlType() {
        return sqlType;
    }
}
