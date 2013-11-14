package nl.astraeus.database;

import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 9:04 PM
 */
public class DatabaseDialect {

    private static Map<Class<?>, String> mapping;

    public String getColumnDescription(Class<?> javaType) {
        return mapping.get(javaType);
    }


}
