package nl.astraeus.database.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 11/16/13
 * Time: 11:07 AM
 */
public class Cache {

    private static Cache instance = new Cache();

    public static Cache get() {
        return instance;
    }

    private Map<Class<?>, ObjectCache<?>> cache = new ConcurrentHashMap<>();

    public boolean inCache(Class<?> cls, Long id) {
        return cache.get(cls) != null && cache.get(cls).knownObject(id);
    }

    public <T> T get(Class<T> cls, Long id) {
        T result = null;

        ObjectCache objectCache = cache.get(cls);

        if (objectCache != null) {
            result = (T) objectCache.getObject(id);
        }

        return result;
    }

    public <T> void set(Class<T> cls, Long id, T object) {
        ObjectCache<T> objectCache = (ObjectCache<T>) cache.get(cls);

        if (objectCache == null) {
            objectCache = new ObjectCache<T>();

            cache.put(cls, objectCache);
        }

        objectCache.setObject(id, object);
    }

}
