package nl.astraeus.database;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 11/15/13
 * Time: 10:45 PM
 */
public class MetaDataHandler {

    private static MetaDataHandler instance = new MetaDataHandler();

    public static MetaDataHandler get() {
        return instance;
    }

    private Map<Class<?>, MetaData> metaData = new HashMap<>();

    public MetaData getMetaData(Class<?> cls) {
        MetaData result = metaData.get(cls);

        if (result == null) {
            synchronized (MetaDataHandler.class) {
                result = metaData.get(cls);

                if (result == null) {
                    result = new MetaData(cls);

                    metaData.put(cls, result);
                }
            }
        }

        return result;
    }
}
