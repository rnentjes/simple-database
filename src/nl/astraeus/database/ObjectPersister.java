package nl.astraeus.database;

import java.util.List;

import nl.astraeus.database.cache.Cache;

/**
 * Date: 11/13/13
 * Time: 9:36 PM
 */
public class ObjectPersister<T> {

    private Class<T>    cls;
    private MetaData    metaData;
    private Cache       cache;

    public ObjectPersister(Class<T> cls, MetaData<T> metaData, Cache cache) {
        this.cls = cls;
        this.metaData = metaData;
        this.cache = cache;
    }

    public void insert(T object) {
        metaData.insert(object);

        Long id = metaData.getId(object);
        cache.set((Class<Object>) object.getClass(), id, object);
    }

    public void update(T object) {
        metaData.update(object);

        Long id = metaData.getId(object);
        cache.set((Class<Object>) object.getClass(), id, object);
    }

    public void delete(T object) {
        Long id = metaData.getId(object);

        metaData.delete(id);
        cache.set(object.getClass(), id, null);
    }

    public T find(long id) {
        T result = (T) metaData.find(id);

        cache.set(cls, id, result);

        return result;
    }

    public List<T> selectAll() {
        return metaData.selectAll();
    }

    public List<T> selectFrom(String query, Object... params) {
        return metaData.selectFrom(query, params);
    }

    public List<T> selectWhere(String query, Object ... params) {
        return metaData.selectWhere(query, params);
    }

    public List<T> selectWhere(int from, int max, String query, Object ... params) {
        return metaData.selectWhere(from, max, query, params);
    }

    public int selectCount(String query, Object ... params) {
        return metaData.selectCount(query, params);
    }

    public T findWhere(String query, Object ... params) {
        return (T) metaData.findWhere(query, params);
    }

}
