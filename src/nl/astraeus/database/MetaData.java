package nl.astraeus.database;

import nl.astraeus.database.annotations.Cache;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;
import nl.astraeus.template.SimpleTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Date: 11/13/13
 * Time: 9:41 PM
 */
public class MetaData<T> {
    private final static Logger logger = LoggerFactory.getLogger(MetaData.class);

    private Class<T> cls;
    private String tableName;
    private FieldMetaData pk = null;
    private FieldMetaData [] fieldsMetaData;
    private String insertSql;
    private String selectSql;
    private String updateSql;
    private String deleteSql;

    private ThreadLocal<Map<Class<?>, Map<Long, Object>>> circularReferences = new ThreadLocal<>();

    public MetaData(Class<T> cls) {
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
            nl.astraeus.database.cache.Cache.get().setMaxAge(cls, cache.maxAge());
        }

        Field [] fields = cls.getDeclaredFields();
        List<FieldMetaData> fieldMeta = new ArrayList<>();

        for (Field field : fields) {
            if (!field.getName().contains("jacoco")) {
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
                for (FieldMetaData meta : fieldsMetaData) {
                    ResultSet columnMetaData = connection.getMetaData().getColumns(null, null, tableName, meta.getColumnInfo().getName());

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
     }

    private void createColumn(FieldMetaData meta) {
        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("column", meta.getColumnInfo());

        SimpleTemplate template = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.CREATE_COLUMN);

        execute(template, model);
    }

    private void execute(SimpleTemplate createTemplate, Map<String, Object> model) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = Persister.getNewConnection();

            String sql = createTemplate.render(model);
            logger.info("Executing:\n" + sql);
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
        return executeInNewConnection(new ExecuteConnectionWithResult<T>() {
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

        result = executeInNewConnection(new ExecuteConnectionWithResult<List<T>>() {
            @Override
            public List<T> execute(Connection connection) throws SQLException {
                List<T> result = new ArrayList<>();
                PreparedStatement statement = null;

                try {
                    statement = connection.prepareStatement(fromSql);
                    int index = 1;

                    for (Object param : params) {
                        setStatementParameter(statement, index++, param);
                    }

                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        Long id = rs.getLong(1);

                        result.add((T) Persister.find(cls, id));
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

    public <T> List<T> selectWhere(String query, final Object[] params) {
        List<T> result;
        SimpleTemplate whereTemplate = DdlMapping.get().getQueryTemplate(DdlMapping.QueryTemplates.SELECT_WHERE);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", tableName);
        model.put("key", pk.getColumnInfo().getName());
        model.put("query", query);
        final String whereSql = whereTemplate.render(model);

        result = executeInNewConnection(new ExecuteConnectionWithResult<List<T>>() {
            @Override
            public List<T> execute(Connection connection) throws SQLException {
                List<T> result = new ArrayList<>();
                PreparedStatement statement = null;

                try {
                    statement = connection.prepareStatement(whereSql);
                    int index = 1;

                    logger.info("WhereSql: "+whereSql);

                    for (Object param : params) {
                        setStatementParameter(statement, index++, param);
                    }

                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        Long id = rs.getLong(1);

                        result.add((T) Persister.find(cls, id));
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

    private void setStatementParameter(PreparedStatement statement, int index, Object param) throws SQLException {
        if (param.getClass().equals(String.class)) {
            statement.setString(index, (String) param);
        } else if (param.getClass().equals(Long.class) || param.getClass().equals(long.class)) {
            statement.setLong(index, (Long) param);
        } else if (param.getClass().equals(Integer.class) || param.getClass().equals(int.class)) {
            statement.setInt(index, (Integer) param);
        } else if (param.getClass().equals(Short.class) || param.getClass().equals(short.class)) {
            statement.setShort(index, (Short)param);
        } else if (param.getClass().equals(Double.class) || param.getClass().equals(double.class)) {
            statement.setDouble(index, (Double)param);
        } else if (param.getClass().equals(BigDecimal.class)) {
            statement.setBigDecimal(index, (BigDecimal) param);
        } else if (param.getClass().equals(Boolean.class) || param.getClass().equals(boolean.class)) {
            statement.setBoolean(index, (Boolean) param);
        } else if (param.getClass().equals(Date.class)) {
            Timestamp date = new java.sql.Timestamp(((Date)param).getTime());

            statement.setTimestamp(index, date);
        } else {
            throw new IllegalStateException("Type "+param.getClass()+" not supported in where queries yet!");
        }
    }

    private void executeInNewConnection(ExecuteConnection es) {
        Connection connection = null;

        try {
            connection = Persister.getNewConnection();

            es.execute(connection);
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

    private <T> T executeInNewConnection(ExecuteConnectionWithResult<T> es) {
        Connection connection = null;

        try {
            connection = Persister.getNewConnection();

            return es.execute(connection);
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

    public Long getId(Object object) {
        return (Long)pk.get(object);
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
