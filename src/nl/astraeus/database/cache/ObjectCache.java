package nl.astraeus.database.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 10:59 AM
 */
public class ObjectCache<T> {

    private LinkedHashMap<Long, ObjectReference<T>> cache = new LinkedHashMap<Long, ObjectReference<T>>(1000, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, ObjectReference<T>> eldest) {
            return size() > ObjectCache.this.getMaxSize();
        }
    };

    private int maxSize;

    public ObjectCache(int maxSize) {
        this.maxSize = maxSize;
    }

    protected boolean inCache(Long id) {
        ObjectReference<T> ref = cache.get(id);

        return ref != null;
    }

    protected T getObject(Long id) {
        T result = null;
        ObjectReference<T> ref = cache.get(id);

        if (ref != null) {
            result = ref.get();
        }

        return result;
    }

    protected void setObject(Long id, T object) {
        ObjectReference<T> ref = cache.get(id);

        if (ref == null) {
            ref = new ObjectReference<T>(id, object);
        } else {
            ref.set(object);
        }

        cache.put(id, ref);
    }

    public int getNumberCached() {
        return cache.size();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void clear() {
        cache.clear();
    }

}
