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
import java.sql.Statement;
import java.util.*;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData {
    private final static Logger logger = LoggerFactory.getLogger(MetaData.class);

    private Class<?> cls;
    private String tableName;
    private FieldMetaData pk = null;
    private FieldMetaData [] fieldsMetaData;
    private String insertSql;

    public MetaData(Class<?> cls) {
        this.cls = cls;

        processAnnotation(cls.getAnnotation(Table.class));

        if (tableName == null) {
            tableName = cls.getSimpleName();
        }

        Field [] fields = cls.getDeclaredFields();
        this.fieldsMetaData = new FieldMetaData[fields.length];
        int index = 0;

        for (Field field : fields) {
            FieldMetaData info = new FieldMetaData(field);

            fieldsMetaData[index] = info;

            if (field.getAnnotation(Id.class) != null) {
                if (!field.getType().equals(Long.class) &&
                    !field.getType().equals(long.class) &&
                    !field.getType().equals(Integer.class) &&
                    !field.getType().equals(int.class)) {

                    throw new IllegalStateException("PK Field must be long or integer! ("+field.getDeclaringClass().getSimpleName()+"."+field.getName()+")");
                } else if (pk != null) {
                    throw new IllegalStateException("Compound primary keys not supported, multable id field defined in "+field.getDeclaringClass().getSimpleName());
                }

                pk = info;
            }

            index++;
        }

        try {
            // get metadata from database
            ResultSet result = Persister.getConnection().getMetaData().getTables(null, null, tableName.toUpperCase(), null);

            if (result.next()) {
                for (FieldMetaData meta : fieldsMetaData) {
                    ResultSet columnMetaData = Persister.getConnection().getMetaData().getColumns(null, null, tableName.toUpperCase(), meta.getColumnInfo().getName().toUpperCase());

                    if (columnMetaData.next()) {
                        if (meta.isPrimaryKey()) {

                        }
                        // check type etc
                        // warn if different
                    } else {
                        // create Column....
                        createColumn(meta);
                    }
                }
            } else {
                createTable();
            }

            SimpleTemplate insertTemplate = TemplateHandler.get().getInsertTemplate();

            Map<String, Object> model = new HashMap<>();

            List<String> columns = new LinkedList<>();

            for (FieldMetaData meta : fieldsMetaData) {
                if (!meta.isPrimaryKey()) {
                    columns.add(meta.getColumnInfo().getName());
                }
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

        for (FieldMetaData meta : fieldsMetaData) {
            if (!meta.isPrimaryKey()) {
                columns.add(meta.getColumnInfo());
            }
        }

        model.put("tableName", tableName);
        model.put("columns", columns);
        model.put("key", pk.getColumnInfo().getName());

        SimpleTemplate template = TemplateHandler.get().getCreateTemplate();

        execute(template, model);
     }

    private void createColumn(FieldMetaData meta) {
        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("column", meta.getColumnInfo());

        SimpleTemplate template = TemplateHandler.get().getCreateColumnTemplate();

        execute(template, model);
    }

    private void execute(SimpleTemplate createTemplate, Map<String, Object> model) {
        PreparedStatement statement = null;

        try {
            String sql = createTemplate.render(model);
            System.out.println("Executing:\n"+sql);
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

    protected <T> void insert(T object) {
        PreparedStatement statement = null;

        try {
            statement = Persister.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;

            for (FieldMetaData meta : fieldsMetaData) {
                if (!meta.isPrimaryKey()) {
                    meta.set(statement, index++, object);
                }
            }

            statement.execute();

            ResultSet rs = statement.getGeneratedKeys();

            if (rs != null && rs.next()) {
                pk.set(object, rs.getLong(1));
            }
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

    private <T> void setId(T object, Long key) {
        //To change body of created methods use File | Settings | File Templates.
    }

}
