package nl.astraeus.database;

import nl.astraeus.database.cache.Cache;

import java.util.LinkedList;
import java.util.List;

/**
 * Date: 11/13/13
 * Time: 9:36 PM
 */
public class ObjectPersister<T> {

    private Class<T> cls;
    private MetaData metaData;

    public ObjectPersister(Class<T> cls) {
        this.cls = cls;
        this.metaData = MetaDataHandler.get().getMetaData(cls);
    }

    public void insert(Object object) {
        metaData.insert(object);
    }

    public void update(Object object) {
        metaData.update(object);
    }

    public void delete(Object object) {
        Long id = metaData.getId(object);

        metaData.delete(id);

        Cache.get().set(object.getClass(), id, null);
    }

    public T find(long id) {
        if (Cache.get().inCache(cls, id)) {
            return Cache.get().get(cls, id);
        }

        T result = (T) metaData.find(id);

        Cache.get().set(cls, id, result);

        return result;
    }

    public List<T> select(String query, Object ... params) {
        return new LinkedList<>();

    }

    public List<T> selectWhere(String query, Object ... params) {
        return metaData.selectWhere(query, params);
    }

}
