package nl.astraeus.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 11/15/13
 * Time: 10:45 PM
 */
public class MetaDataHandler {
    private final static Logger logger = LoggerFactory.getLogger(MetaDataHandler.class);

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
                    long start = System.nanoTime();

                    result = new MetaData<>(cls);

                    logger.info("Getting metadata for class {} took {}ms", cls.getSimpleName(), (System.nanoTime() - start) / 1000000f);

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
