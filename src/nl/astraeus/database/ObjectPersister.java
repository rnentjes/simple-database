package nl.astraeus.database;

import java.sql.Connection;

/**
 * Date: 11/13/13
 * Time: 9:36 PM
 */
public class ObjectPersister {

    private Class<?> cls;

    public ObjectPersister(Class<?> cls) {
        this.cls = cls;
    }

    public void store(Connection connection, Object object) {

    }

}
