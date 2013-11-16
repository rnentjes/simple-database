package nl.astraeus.database.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date: 11/16/13
 * Time: 10:59 AM
 */
public class ObjectCache<T> {

    private Map<Long, ObjectReference<T>> cache = new ConcurrentHashMap<>();

    private int maxSize = 100;
    private long maxAge = 0;

    public boolean knownObject(Long id) {
        return cache.get(id) != null;
    }

    public T getObject(Long id) {
        T result = null;
        ObjectReference<T> ref = cache.get(id);

        if (ref != null) {
            result = ref.get();
        }

        return result;
    }

    public void setObject(Long id, T object) {
        ObjectReference<T> ref = cache.get(id);

        if (ref == null) {
            ref = new ObjectReference<T>(object);

            cache.put(id, ref);
        } else {
            ref.set(object);
        }
    }

    public int getNumberCached() {
        return cache.size();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
