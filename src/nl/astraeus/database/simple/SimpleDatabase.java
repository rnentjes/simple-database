package nl.astraeus.database.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.astraeus.database.ObjectPersister;
import nl.astraeus.database.jdbc.ConnectionProvider;

/**
 * User: rnentjes
 * Date: 10-12-16
 * Time: 14:50
 */
public class SimpleDatabase {
    private static Map<Class<?>, ObjectPersister> objectPersisters = new HashMap<>();
    protected static Map<String, SimpleDatabase> databases = new ConcurrentHashMap<>();

    public static SimpleDatabase get() {
        return get("default");
    }

    public static SimpleDatabase get(String name) {
        SimpleDatabase result = databases.get(name);

        if (result == null) {
            throw new IllegalStateException("Database with name '"+name+"' is not defined!");
        }

        return result;
    }

    public static SimpleDatabase define(ConnectionProvider provider) {
        return define("default", provider);
    }

    public static SimpleDatabase define(String name, ConnectionProvider provider) {
        SimpleDatabase result = databases.get(name);

        if (result != null) {
            throw new IllegalStateException("Database with name '"+name+"' is already defined!");
        }

        SimpleDatabase database = new SimpleDatabase(provider);

        databases.put(name, database);

        return database;
    }

    private ConnectionProvider connectionProvider;
    private boolean executeDDLUpdates;

    private SimpleDatabase(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.executeDDLUpdates = false;
    }

    public void setExecuteDDLUpdates(boolean executeDDLUpdates) {
        this.executeDDLUpdates = executeDDLUpdates;
    }

//    public void execute(Executor executor) {
//
//    }
}
