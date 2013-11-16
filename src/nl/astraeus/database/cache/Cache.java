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
        return cache.get(cls) != null && cache.get(cls).inCache(id);
    }

    public <T> T get(Class<T> cls, Long id) {
        return getObjectCache(cls).getObject(id);
    }

    public <T> void set(Class<T> cls, Long id, T object) {
        ObjectCache<T> objectCache = (ObjectCache<T>) cache.get(cls);

        if (objectCache == null) {
            objectCache = new ObjectCache<T>();

            cache.put(cls, objectCache);
        }

        objectCache.setObject(id, object);
    }

    private <T> ObjectCache<T> getObjectCache(Class<T> cls) {
        ObjectCache<T> objectCache = (ObjectCache<T>) cache.get(cls);

        if (objectCache == null) {
            objectCache = new ObjectCache<T>();

            cache.put(cls, objectCache);
        }

        return objectCache;
    }

    public <T> void setMaxSize(Class<T> cls, int maxSize) {
        getObjectCache(cls).setMaxSize(maxSize);
    }

    public <T> void setMaxAge(Class<T> cls, long maxAge) {
        getObjectCache(cls).setMaxAge(maxAge);
    }

    public void clear() {
        cache.clear();
    }

    public Map<Class<?>, ObjectCache<?>> getCache() {
        return cache;
    }
}
