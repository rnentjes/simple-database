package nl.astraeus.database;

import java.util.List;

/**
 * User: rnentjes
 * Date: 10-12-16
 * Time: 15:15
 */
public class SimpleDao<T> {

    private SimpleDatabase database;
    private ObjectPersister<T> persister;

    public SimpleDao(Class<T> cls) {
        this(cls, "default");
    }

    public SimpleDao(Class<T> cls, String database) {
        this.database = SimpleDatabase.get(database);
        this.persister = this.database.getObjectPersister(cls);
    }

    public void insert(T object) {
        persister.insert(object);
    }

    public void update(T object) {
        persister.update(object);
    }

    public void upsert(T object) {
        persister.upsert(object);
    }

    public void delete(T object) {
        persister.delete(object);
    }

    public T find(long id) {
        return persister.find(id);
    }

    public T find(String query, Object ... params) {
        return (T) persister.findWhere(query, params);
    }

    public List<T> all() {
        return persister.selectAll();
    }

    public List<T> from(String query, Object... params) {
        return persister.selectFrom(query, params);
    }

    public List<T> where(String query, Object ... params) {
        return persister.selectWhere(query, params);
    }

    public List<T> where(int from, int max, String query, Object ... params) {
        return persister.selectWhere(from, max, query, params);
    }

    public int count(String query, Object ... params) {
        return persister.selectCount(query, params);
    }

    public interface Executor<T> {
        void execute(SimpleDao<T> dao);
    }

    public void execute(Executor<T> executor) {
        try {
            database.begin();

            executor.execute(this);

            database.commit();
        } finally {
            if (database.transactionActive()) {
                database.rollback();
            }
        }
    }

}
