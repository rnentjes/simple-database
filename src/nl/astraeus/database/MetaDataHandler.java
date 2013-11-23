package nl.astraeus.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 11/15/13
 * Time: 10:45 PM
 */
public class MetaDataHandler {

    private static MetaDataHandler instance = new MetaDataHandler();

    public static MetaDataHandler get() {
        return instance;
    }

    private Map<Class<?>, MetaData> metaData = new ConcurrentHashMap<>();

    public <T> MetaData<T> getMetaData(Class<T> cls) {
        MetaData result = metaData.get(cls);

        if (result == null) {
            synchronized (MetaDataHandler.class) {
                result = metaData.get(cls);

                if (result == null) {
                    result = new MetaData<>(cls);

                    metaData.put(cls, result);
                }
            }
        }

        return result;
    }

    /** Used to reset tests */
    public void clear() {
        metaData = new ConcurrentHashMap<>();
    }

}
