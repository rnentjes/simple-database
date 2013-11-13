package nl.astraeus.database;

import nl.astraeus.database.annotations.Default;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Table;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData {

    public static class FieldInfo {
        private Field field;
        private String fieldName;
        private Class javaType;
        private int columnType          = Types.CHAR;
        private String defaultValue     = null;
        private int columnLength        = 255;
//        private int columnPrecision     = 2;
//        private int columnScale         = 12;

        public FieldInfo(Field field) {
            this.field = field;
            this.field.setAccessible(true);

            fieldName = field.getName();
            javaType = field.getType();

            processAnnotation(field.getAnnotation(Length.class));
            processAnnotation(field.getAnnotation(Default.class));
        }

        private void processAnnotation(Length length) {
            if (length != null) {
                columnLength = length.value();
            }
        }

        private void processAnnotation(Default def) {
            if (def != null) {
                defaultValue = def.value();
            }
        }

        public Object get(Object obj) throws IllegalAccessException {
            return field.get(obj);
        }

        public void set(Object obj, Object value) throws IllegalAccessException {
            field.set(obj, value);
        }
    }

    private Class<?> cls;
    private String tableName;
    private Map<Field, FieldInfo> fields;

    public MetaData(Class<?> cls) {
        this.cls = cls;

        processAnnotation(cls.getAnnotation(Table.class));

        if (tableName == null) {
            tableName = cls.getName();
        }

        this.fields = new HashMap<>();

        for (Field field : cls.getDeclaredFields()) {
            FieldInfo info = new FieldInfo(field);

            fields.put(field, info);
        }
    }

    private void processAnnotation(Table table) {
        if (table != null) {
            tableName = table.name();
        }
    }
}
