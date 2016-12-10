package nl.astraeus.database.simple;

/**
 * User: rnentjes
 * Date: 10-12-16
 * Time: 15:15
 */
public class SimpleDao<T> {

    private String database = "default";
    private Class<T> cls;

    public SimpleDao(Class<T> cls) {
        this.cls = cls;
    }

    public SimpleDao(Class<T> cls, String database) {
        this.cls = cls;
        this.database = database;
    }

    public void insert(T object) {
        //SimpleDatabase.get(database).execute(new Execu);
    }
}
