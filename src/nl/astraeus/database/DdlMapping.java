package nl.astraeus.database;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/19/13
 * Time: 10:46 PM
 */
public class DdlMapping {
    private final static Logger logger = LoggerFactory.getLogger(DdlMapping.class);

    public enum QueryTemplates {
        CREATE,
        CREATE_COLUMN,
        CREATE_INDEX,
        INSERT,
        UPDATE,
        DELETE,
        SELECT,
        SELECT_WHERE,
        SELECT_WHERE_PAGED,
        SELECT_FROM
    }

    public enum DatabaseDefinition {
        H2("H2", "h2", true),
        POSTGRESQL("PostgreSQL", "postgresql", false),
        MYSQL("MySql", "mysql", false);

        private String name;
        private String packageName;
        private boolean ddlInUppercase;

        DatabaseDefinition(String name, String packageName, boolean ddlInUppercase) {
            this.name = name;
            this.packageName = packageName;
            this.ddlInUppercase = ddlInUppercase;
        }

        public String getName() {
            return name;
        }

        public String getPackageName() {
            return packageName;
        }

        public boolean ddlNamesInUppercase() {
            return ddlInUppercase;
        }
    }

    private static DdlMapping instance = new DdlMapping();

    public static DdlMapping get() {
        return instance;
    }

    private static Map<Class<?>, SimpleTemplate> ddlMapping;
    private static Map<Class<?>, Class<?>> primitiveToWrapper;
    private static Map<QueryTemplates, SimpleTemplate> queryTemplates;

    static {
        primitiveToWrapper = new HashMap<>();

        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(void.class, Void.class);
    }

    private DatabaseDefinition database;
    private boolean executeDdlUpdates = false;

    public DdlMapping() {
        setDatabaseType(DatabaseDefinition.H2);
    }

    public void setDatabaseType(DatabaseDefinition definition) {
        database = definition;
        reload();
    }

    public void setExecuteDDLUpdates(boolean eddlup) {
        executeDdlUpdates = eddlup;
    }

    public boolean isExecuteDdlUpdates() {
        return executeDdlUpdates;
    }

    private String findSqlResource(String name) {
        try {
            InputStream in = DdlMapping.class.getResourceAsStream("sql/"+database.getPackageName()+"/"+name);

            if (in == null) {
                in = DdlMapping.class.getResourceAsStream("sql/def/"+name);
            }

            return Util.readAsString(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void reload() {
        ddlMapping = new HashMap<>();
        queryTemplates = new HashMap<>();

        try {
            String types = findSqlResource("types.sql");

            String [] lines = types.split("\\n");

            for (String line : lines) {
                String [] parts = line.split("\\=");

                if (parts.length != 2) {
                    logger.warn("Skipped line: ["+line+"] while loading mapping definition of types.sql");
                }

                Class cls = Class.forName(parts[0]);
                ddlMapping.put(cls, new SimpleTemplate("${", "}", EscapeMode.NONE, parts[1]));
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        queryTemplates.put(QueryTemplates.CREATE,               new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("create.sql")));
        queryTemplates.put(QueryTemplates.CREATE_COLUMN,        new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("createColumn.sql")));
        queryTemplates.put(QueryTemplates.CREATE_INDEX,         new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("createIndex.sql")));
        queryTemplates.put(QueryTemplates.INSERT,               new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("insert.sql")));
        queryTemplates.put(QueryTemplates.UPDATE,               new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("update.sql")));
        queryTemplates.put(QueryTemplates.DELETE,               new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("delete.sql")));
        queryTemplates.put(QueryTemplates.SELECT,               new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("select.sql")));
        queryTemplates.put(QueryTemplates.SELECT_WHERE,         new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("selectWhere.sql")));
        queryTemplates.put(QueryTemplates.SELECT_WHERE_PAGED,   new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("selectWherePaged.sql")));
        queryTemplates.put(QueryTemplates.SELECT_FROM,          new SimpleTemplate("${", "}", EscapeMode.NONE, findSqlResource("selectFrom.sql")));
    }

    public SimpleTemplate getDdlTemplateForType(Class<?> type) {
        if (type.isPrimitive()) {
            type = primitiveToWrapper.get(type);
        }

        // todo: check for null

        return ddlMapping.get(type);
    }

    public SimpleTemplate getBlobType() {
        return getDdlTemplateForType(Object.class);
    }

    public SimpleTemplate getIdType() {
        return getDdlTemplateForType(Long.class);
    }

    public SimpleTemplate getQueryTemplate(QueryTemplates type) {
        return queryTemplates.get(type);
    }

    public boolean ddlNamesInUppercase() {
        return database.ddlNamesInUppercase();
    }
}
