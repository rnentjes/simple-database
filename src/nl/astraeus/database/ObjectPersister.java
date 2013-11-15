package nl.astraeus.database;

/**
 * Date: 11/13/13
 * Time: 9:36 PM
 */
public class ObjectPersister {

    private Class<?> cls;
    private MetaData metaData;

    public ObjectPersister(Class<?> cls) {
        this.cls = cls;
        this.metaData = MetaDataHandler.get().getMetaData(cls);
    }

    public void insert(Object object) {
        // get insert sql
        // fill prepared statement from object


    }

    public void update(Object object) {

    }

    public void delete(Object object) {

    }

    public Object find(long id) {
        return null;
    }

}
