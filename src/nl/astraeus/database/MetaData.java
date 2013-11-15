package nl.astraeus.database;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;
import nl.astraeus.database.sql.TemplateHandler;
import nl.astraeus.template.SimpleTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData {
    private final static Logger logger = LoggerFactory.getLogger(MetaData.class);

    private Class<?> cls;
    private String tableName;
    private Field [] fields;
    private Field pk = null;
    private Map<Field, FieldMetaData> fieldsMetaData;
    private String insertSql;

    public MetaData(Class<?> cls) {
        this.cls = cls;

        processAnnotation(cls.getAnnotation(Table.class));

        if (tableName == null) {
            tableName = cls.getSimpleName();
        }

        this.fieldsMetaData = new HashMap<>();
        this.fields = cls.getDeclaredFields();

        for (Field field : fields) {
            FieldMetaData info = new FieldMetaData(field);

            fieldsMetaData.put(field, info);

            if (field.getAnnotation(Id.class) != null) {
                if (!field.getType().equals(Long.class) &&
                    !field.getType().equals(long.class) &&
                    !field.getType().equals(Integer.class) &&
                    !field.getType().equals(int.class)) {

                    throw new IllegalStateException("PK Field must be long or integer! ("+field.getDeclaringClass().getSimpleName()+"."+field.getName()+")");
                } else if (pk != null) {
                    throw new IllegalStateException("Compound primary keys not supported, multable id field defined in "+field.getDeclaringClass().getSimpleName());
                }

                pk = field;
            }
        }

        try {
            // get metadata from database
            ResultSet result = Persister.getConnection().getMetaData().getTables(null, null, tableName.toUpperCase(), null);

            if (result.next()) {
                for (Field field : fields) {
                    FieldMetaData meta = fieldsMetaData.get(field);

                    ResultSet columnMetaData = Persister.getConnection().getMetaData().getColumns(null, null, tableName.toUpperCase(), meta.getColumnInfo().getName().toUpperCase());

                    if (columnMetaData.next()) {
                        if (pk.equals(field)) {

                        }
                        // check type etc
                        // warn if different
                    } else {
                        // create Column....
                        createColumn(field);
                    }
                }
            } else {
                createTable();
            }

            SimpleTemplate insertTemplate = TemplateHandler.get().getInsertTemplate();

            Map<String, Object> model = new HashMap<>();

            List<String> columns = new LinkedList<>();

            for (Field field : fields) {
                columns.add(fieldsMetaData.get(field).getColumnInfo().getName());
            }

            model.put("tableName", tableName);
            model.put("columns", columns);

            insertSql = insertTemplate.render(model);
            System.out.println(insertSql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    private void processAnnotation(Table table) {
        if (table != null && !table.name().isEmpty() ) {
            tableName = table.name();
        }
    }

    private void createTable() {
        Map<String, Object> model = new HashMap<>();

        List<ColumnInfo> columns = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        for (Field field : fields) {
            FieldMetaData meta = fieldsMetaData.get(field);

            columns.add(meta.getColumnInfo());
        }

        FieldMetaData meta = fieldsMetaData.get(pk);

        keys.add(meta.getColumnInfo().getName());

        model.put("tableName", tableName);
        model.put("columns", columns);
        model.put("keys", keys);

        SimpleTemplate template = TemplateHandler.get().getCreateTemplate();

        execute(template, model);
     }

    private void createColumn(Field column) {
        Map<String, Object> model = new HashMap<>();

        FieldMetaData meta = fieldsMetaData.get(column);

        model.put("tableName", tableName);
        model.put("column", meta.getColumnInfo());

        SimpleTemplate template = TemplateHandler.get().getCreateColumnTemplate();

        execute(template, model);
    }

    private void execute(SimpleTemplate createTemplate, Map<String, Object> model) {
        PreparedStatement statement = null;

        try {
            String sql = createTemplate.render(model);
            statement = Persister.getConnection().prepareStatement(sql);

            statement.execute();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

}
