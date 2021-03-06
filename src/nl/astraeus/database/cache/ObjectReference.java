package nl.astraeus.database.cache;

/**
 * Date: 11/16/13
 * Time: 11:00 AM
 */
public class ObjectReference<T> {

    private long id;
    private T object;
    private long cachedTime;
    private long lastAccessTime;
    private long reads, writes;

    public ObjectReference(long id, T object) {
        this.id = id;
        this.object = object;
        this.cachedTime = System.currentTimeMillis();
        this.reads = 0;
        this.writes = 0;
    }

    public long getId() {
        return id;
    }

    public T get() {
        reads++;
        lastAccessTime = System.currentTimeMillis();
        return object;
    }

    public void set(T object) {
        writes++;
        lastAccessTime = System.currentTimeMillis();
        this.object = object;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getCachedTime() {
        return cachedTime;
    }

    public long getReads() {
        return reads;
    }

    public long getWrites() {
        return writes;
    }
}
