package nl.astraeus.database;

import nl.astraeus.database.annotations.Default;
import nl.astraeus.database.annotations.Length;

import java.lang.reflect.Field;

/**
 * Date: 11/14/13
 * Time: 8:59 PM
 */
public class FieldMetaData {

    private Field field;
    private String fieldName;
    private Class javaType;

    private ColumnInfo  columnInfo;

    private Length length;
    private Default defaultValue;

    public FieldMetaData(Field field) {
        this.field = field;
        this.field.setAccessible(true);

        fieldName = field.getName();
        javaType = field.getType();

        length = field.getAnnotation(Length.class);
        defaultValue = field.getAnnotation(Default.class);
    }

    public Object get(Object obj) throws IllegalAccessException {
        return field.get(obj);
    }

    public void set(Object obj, Object value) throws IllegalAccessException {
        field.set(obj, value);
    }
}
