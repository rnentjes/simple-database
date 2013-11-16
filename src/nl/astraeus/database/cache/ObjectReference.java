package nl.astraeus.database.cache;

/**
 * Date: 11/16/13
 * Time: 11:00 AM
 */
public class ObjectReference<T> {

    private T object;
    private long cachedTime;
    private long reads, writes;

    public ObjectReference(T object) {
        this.object = object;
        this.cachedTime = System.currentTimeMillis();
        this.reads = 0;
        this.writes = 0;
    }

    public T get() {
        reads++;
        return object;
    }

    public void set(T object) {
        writes++;
        this.object = object;
    }

}
