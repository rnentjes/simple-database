package nl.astraeus.database;

import nl.astraeus.database.annotations.*;
import nl.astraeus.database.util.ReferenceGenerator;
import nl.astraeus.template.SimpleTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData<M> {
    private final static Logger logger = LoggerFactory.getLogger(MetaData.class);

    private Class<M> cls;
    private String tableName;
    private FieldMetaData pk = null;
    private FieldMetaData [] fieldsMetaData;
    private String insertSql;
    private String selectSql;
    private String updateSql;
    private String deleteSql;

    private ThreadLocal<Map<Class<?>, Map<Long, Object>>> circularReferences = new ThreadLocal<>();

    public MetaData(Class<M> cls) {
        this.cls = cls;

        processAnnotation(cls.getAnnotation(Table.class));

        if (tableName == null) {
            tableName = cls.getSimpleName();
        }

        if (DdlMapping.get().ddlNamesInUppercase()) {
            tableName = tableName.toUpperCase();
        }

        Cache cache = cls.getAnnotation(Cache.class);

        if (cache != null) {
            nl.astraeus.database.cache.Cache.get().setMaxSize(cls, cache.maxSize());
        }

        Field [] fields = cls.getDeclaredFields();
        List<FieldMetaData> fieldMeta = new ArrayList<>();

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            Transient trans = field.getAnnotation(Transient.class);
            if (!field.getName().contains("jacoco") && trans == null && !Modifier.isStatic(modifiers)) {
                FieldMetaData info = new FieldMetaData(this, field);

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
            connection = Persister.getNewConnection();
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
                    } else if (DdlMapping.get().isExecuteDdlUpdates()) {
                        // create Column....
                        createColumn(meta);
                    } else {
                        throw new IllegalStateException("Column "+cls.getSimpleName()+"."+meta.getFieldName()+" not found in table "+tableName);
                    }
                }
            } else if (DdlMapping.get().isExecuteDdlUpdates()) {
                createTable();
            } else {
                throw new IllegalStateException("Table "+tableName+" not found for class "+cls.getSimpleName());
            }

            SimpleTemplate insertTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.INSERT);
            SimpleTemplate selectTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.SELECT);
            SimpleTemplate updateTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.UPDATE);
            SimpleTemplate deleteTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.DELETE);

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

        SimpleTemplate template = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.CREATE);

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

        SimpleTemplate template = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.CREATE_COLUMN);

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

        SimpleTemplate template = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.CREATE_INDEX);

        execute(template, model);
    }

    private void execute(SimpleTemplate createTemplate, Map<String, Object> model) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Persister.getNewConnection();

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

    protected <T> T find(final Long id) {
        return execute(new ExecuteConnectionWithResult<T>() {
            @Override
            public T execute(Connection connection) throws SQLException {
                PreparedStatement statement = null;

                try {
                    T result = null;
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

    private <T> T getFromResultSet(ResultSet rs) {
        T result;

        try {
            result = (T)cls.newInstance();
            int index = 1;

            for (FieldMetaData meta : fieldsMetaData) {
                meta.set(rs, index++, result);
            }

            return result;
        } catch (InstantiationException | SQLException | IllegalAccessException e) {
            throw new IllegalStateException (e);
        }
    }

    protected <T> void insert(T object) {
        PreparedStatement statement = null;

        try {
            statement = Persister.getConnection().prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
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

    protected <T> void update(T object) {
        PreparedStatement statement = null;

        try {
            statement = Persister.getConnection().prepareStatement(updateSql);
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
            statement = Persister.getConnection().prepareStatement(deleteSql);

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

    public <T> List<T> selectAll() {
        return selectFrom("order by "+pk.getColumnInfo().getName(), new Object [0]);
    }


    public <T> List<T> selectFrom(String query, final Object[] params) {
        List<T> result;
        SimpleTemplate fromTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.SELECT_FROM);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);

        final String fromSql = fromTemplate.render(model);

        result = execute(new ExecuteSelect<T>(cls, fromSql, params));

        return result;
    }

    public <T> List<T> selectWhere(String query, final Object[] params) {
        List<T> result;
        SimpleTemplate whereTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);

        final String whereSql = whereTemplate.render(model);

        result = execute(new ExecuteSelect<T>(cls, whereSql, params));

        return result;
    }

    public <T> List<T> selectWhere(int from, int max, String query, final Object[] params) {
        List<T> result;
        SimpleTemplate whereTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE_PAGED);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);
        model.put("order", query);
        model.put("from", from);
        model.put("to", from+max);
        model.put("max", max);

        final String whereSql = whereTemplate.render(model);

        result = execute(new ExecuteSelect<T>(cls, whereSql, params));

        return result;
    }

    public int selectCount(String query, final Object[] params) {
        Integer result;
        SimpleTemplate whereTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE);

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

    public <T> T findWhere(String query, final Object[] params) {
        List<T> results = selectWhere(query, params);
        T result = null;

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
            if (Persister.transactionActive()) {
                connection = Persister.getConnection();
            } else {
                connection = Persister.getNewConnection();
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
