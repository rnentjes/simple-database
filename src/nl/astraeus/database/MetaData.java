package nl.astraeus.database;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.astraeus.database.annotations.Cache;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Reference;
import nl.astraeus.database.annotations.Table;
import nl.astraeus.database.annotations.Transient;
import nl.astraeus.database.util.ReferenceGenerator;
import nl.astraeus.template.SimpleTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData<M> {
    private final static Logger logger = LoggerFactory.getLogger(MetaData.class);

    private Class<M> cls;
    private SimpleDatabase db;
    private DdlMapping ddlMapping;

    private String tableName;
    private FieldMetaData pk = null;
    private FieldMetaData [] fieldsMetaData;
    private String insertSql;
    private String selectSql;
    private String updateSql;
    private String deleteSql;

    private ThreadLocal<Map<Class<?>, Map<Long, Object>>> circularReferences = new ThreadLocal<>();

    public MetaData(Class<M> cls, SimpleDatabase database) {
        this.cls = cls;
        this.db = database;
        this.ddlMapping = database.getDdlMapping();

        processAnnotation(cls.getAnnotation(Table.class));

        if (tableName == null) {
            tableName = cls.getSimpleName();
        }

        if (ddlMapping.ddlNamesInUppercase()) {
            tableName = tableName.toUpperCase();
        }

        Cache cache = cls.getAnnotation(Cache.class);

        if (cache != null) {
            db.getCache().setMaxSize(cls, cache.maxSize());
        }

        Field [] fields = cls.getDeclaredFields();
        List<FieldMetaData> fieldMeta = new ArrayList<>();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            Transient trans = field.getAnnotation(Transient.class);
            if (!field.getName().contains("jacoco") && trans == null && !Modifier.isStatic(modifiers)) {
                FieldMetaData info = new FieldMetaData(database, field);

                fieldMeta.add(info);

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
            }
        }

        this.fieldsMetaData = fieldMeta.toArray(new FieldMetaData[fieldMeta.size()]);

        Connection connection = null;
        try {
            // get metadata from database
            connection = db.getNewConnection();
            ResultSet result = connection.getMetaData().getTables(null, null, tableName, null);

            if (result.next()) {
                Map<String, ColumnMetaData> columnMetaData = getColumnMetaData(connection);

                for (FieldMetaData meta : fieldsMetaData) {
                    ColumnMetaData cmd = columnMetaData.get(meta.getColumnInfo().getName());
                    if (cmd != null) {
                        if (meta.isPrimaryKey()) {

                        }
                        // check type etc
                        if (cmd.getSqlType() != null && !cmd.getSqlType().equals(meta.getSqlType())) {
                            throw new IllegalStateException("Field " + cls.getSimpleName() + "." + meta.getFieldName() + " doesn't match type for column " + tableName + "." + cmd.getName());
                        }
                        // (re)create index
                        if (meta.hasIndex()) {
                            createIndexes(meta);
                        }
                    } else if (db.isExecuteDdlUpdates()) {
                        // create Column....
                        createColumn(meta);
                    } else {
                        throw new IllegalStateException("Column "+cls.getSimpleName()+"."+meta.getFieldName()+" not found in table "+tableName);
                    }
                }
            } else if (db.isExecuteDdlUpdates()) {
                createTable();
            } else {
                throw new IllegalStateException("Table "+tableName+" not found for class "+cls.getSimpleName());
            }

            SimpleTemplate insertTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.INSERT);
            SimpleTemplate selectTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.SELECT);
            SimpleTemplate updateTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.UPDATE);
            SimpleTemplate deleteTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.DELETE);

            Map<String, Object> model = new HashMap<>();

            List<String> columns = new LinkedList<>();

            for (FieldMetaData meta : fieldsMetaData) {
                if (!meta.isPrimaryKey()) {
                    columns.add(meta.getColumnInfo().getName());
                }
            }

            model.put("tableName", tableName);
            model.put("columns", columns);
            model.put("key", pk.getColumnInfo().getName());

            insertSql = insertTemplate.render(model);
            updateSql = updateTemplate.render(model);
            deleteSql = deleteTemplate.render(model);

            columns.add(0, pk.getColumnInfo().getName());
            selectSql = selectTemplate.render(model);

            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private Map<String, ColumnMetaData> getColumnMetaData(Connection connection) throws SQLException {
        Map<String, ColumnMetaData> result = new HashMap<>();
        ResultSet columnsMetaData = connection.getMetaData().getColumns(null, null, tableName, null);

        while(columnsMetaData.next()) {
            ColumnMetaData cmd = new ColumnMetaData(
                    columnsMetaData.getString("COLUMN_NAME"),
                    columnsMetaData.getInt("DATA_TYPE"),
                    columnsMetaData.getInt("COLUMN_SIZE"),
                    columnsMetaData.getInt("DECIMAL_DIGITS")
            );

            result.put(cmd.getName(), cmd);
        }

        return result;
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

        SimpleTemplate template = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.CREATE);

        execute(template, model);

        for (FieldMetaData meta : fieldsMetaData) {
            if (meta.hasIndex()) {
                createIndexes(meta);
            }
        }
    }

    private void createColumn(FieldMetaData meta) {
        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("column", meta.getColumnInfo());

        SimpleTemplate template = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.CREATE_COLUMN);

        execute(template, model);

        if (meta.hasIndex()) {
            createIndexes(meta);
        }
    }

    private void createIndexes(FieldMetaData meta) {
        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("column", meta.getColumnInfo());
        model.put("unique", meta.hasUniqueIndex());

        SimpleTemplate template = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.CREATE_INDEX);

        execute(template, model);
    }

    private void execute(SimpleTemplate createTemplate, Map<String, Object> model) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = db.getNewConnection();

            String sql = createTemplate.render(model);

            statement = connection.prepareStatement(sql);

            statement.execute();

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            throw new IllegalStateException(e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                try {
                if (connection != null) {
                    connection.close();
                }
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    protected M find(final Long id) {
        return execute(new ExecuteConnectionWithResult<M>() {
            @Override
            public M execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;

                try {
                    M result = null;
                    statement = connection.prepareStatement(selectSql);

                    statement.setLong(1, id);

                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        result = getFromResultSet(rs);
                    }

                    rs.close();

                    return result;
                } finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
            }
        });
    }

    private M getFromResultSet(ResultSet rs) {
        M result;

        try {
            result = cls.newInstance();
            int index = 1;

            for (FieldMetaData meta : fieldsMetaData) {
                meta.set(rs, index++, result);
            }

            return result;
        } catch (InstantiationException | SQLException | IllegalAccessException e) {
            throw new IllegalStateException (e);
        }
    }

    protected void insert(M object) {
        PreparedStatement statement = null;

        try {
            statement = db.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;

            for (FieldMetaData meta : fieldsMetaData) {
                Reference reference = meta.getReference();
                if (reference != null) {
                    meta.set(object, ReferenceGenerator.generateRandomReference(reference.length()));
                }

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

    protected void update(M object) {
        PreparedStatement statement = null;

        try {
            statement = db.getConnection().prepareStatement(updateSql);
            int index = 1;

            for (FieldMetaData meta : fieldsMetaData) {
                if (!meta.isPrimaryKey()) {
                    meta.set(statement, index++, object);
                }
            }

            pk.set(statement, index++, object);

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

    public void delete(Long id) {
        PreparedStatement statement = null;

        try {
            statement = db.getConnection().prepareStatement(deleteSql);

            statement.setLong(1, id);

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

    public List<M> selectAll() {
        return selectFrom("order by "+pk.getColumnInfo().getName(), new Object [0]);
    }


    public List<M> selectFrom(String query, final Object[] params) {
        List<M> result;
        SimpleTemplate fromTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.SELECT_FROM);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);

        final String fromSql = fromTemplate.render(model);

        result = execute(new ExecuteSelect<>(this, fromSql, params));

        return result;
    }

    public List<M> selectWhere(String query, final Object[] params) {
        List<M> result;
        SimpleTemplate whereTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);

        final String whereSql = whereTemplate.render(model);

        result = execute(new ExecuteSelect<M>(this, whereSql, params));

        return result;
    }

    public List<M> selectWhere(int from, int max, String query, final Object[] params) {
        List<M> result;
        SimpleTemplate whereTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE_PAGED);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);
        model.put("order", query);
        model.put("from", from);
        model.put("to", from+max);
        model.put("max", max);

        final String whereSql = whereTemplate.render(model);

        result = execute(new ExecuteSelect<M>(this, whereSql, params));

        return result;
    }

    public int selectCount(String query, final Object[] params) {
        Integer result;
        SimpleTemplate whereTemplate = ddlMapping.getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", "COUNT("+pk.getColumnInfo().getName()+")");
        model.put("query", query);
        final String whereSql = whereTemplate.render(model);

        result = execute(new ExecuteConnectionWithResult<Integer>() {
            @Override
            public Integer execute(Connection connection) throws SQLException {
                Integer result = 0;
                PreparedStatement statement = null;

                try {
                    statement = connection.prepareStatement(whereSql);
                    int index = 1;

                    for (Object param : params) {
                        StatementHelper.setStatementParameter(statement, index++, param);
                    }

                    ResultSet rs = statement.executeQuery();

                    if (rs.next()) {
                        result = rs.getInt(1);
                    }

                    return result;
                } finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
            }
        });

        return result;
    }

    public M findWhere(String query, final Object[] params) {
        List<M> results = selectWhere(query, params);
        M result = null;

        if (results.size() == 1) {
            result = results.get(0);
        } else if (results.size() > 1) {
            throw new IllegalStateException("Finder found more than one row! ["+query+"]");
        }

        return result;
    }

    private <T> T execute(ExecuteConnectionWithResult<T> es) {
        Connection connection = null;
        boolean close = false;

        try {
            if (db.transactionActive()) {
                connection = db.getConnection();
            } else {
                connection = db.getNewConnection();
                close = true;
            }

            return es.execute(connection);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            if (close && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    public Long getId(Object object) {
        Object obj = pk.get(object);

        if (obj instanceof Integer) {
            obj = ((Integer) obj).longValue();
        }

        return (Long)obj;
    }

    public <T> void reloadReferences(T result) {
        for (FieldMetaData field : fieldsMetaData) {
            if (field.getType() == FieldMetaData.ColumnType.REFERENCE) {
                field.reloadReference(result);
            }
        }
    }

    public <T> T clone(T original) {
        try {
            T result = (T) cls.newInstance();

            for (FieldMetaData meta : fieldsMetaData) {
                meta.set(result, meta.get(original));
            }

            return result;
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
