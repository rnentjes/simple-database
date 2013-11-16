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

    public boolean inCache(Long id) {
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
            ref = new ObjectReference<T>(id, object);

            cache.put(id, ref);
        } else {
            ref.set(object);
        }

        if (cache.size() > maxSize) {
            ObjectReference delete = null;

            for (ObjectReference objRef : cache.values()) {
                if (delete == null || delete.getLastAccessTime() > objRef.getLastAccessTime()) {
                    delete = objRef;
                }
            }

            if (delete != null) {
                cache.remove(delete.getId());
            }
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
