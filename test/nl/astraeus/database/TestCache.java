package nl.astraeus.database;

import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.cache.ObjectCache;
import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Info;
import nl.astraeus.database.test.model.Person;

import java.util.List;
import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestCache {

    public static void main(String [] args) {
        List<Person> persons = Persister.selectAll(Person.class);
        List<Company> company = Persister.selectAll(Company.class);
        List<Info> infos = Persister.selectAll(Info.class);

        if (persons.size() == 0) {
            Persister.execute(new Persister.Executor() {
                @Override
                public void execute() {
                    insert(new Person("Rien", 40, "Rozendael"));
                    insert(new Person("Jan", 32, "Straat"));
                    insert(new Person("Piet", 26, "Weg"));
                    insert(new Person("Klaas", 10, "Pad"));
                    insert(new Person("Rien", 40, "Rozendael"));
                    insert(new Person("Jan", 32, "Straat"));
                    insert(new Person("Piet", 26, "Weg"));
                    insert(new Person("Klaas", 10, "Pad"));
                }
            });

            persons = Persister.selectAll(Person.class);
        }

        Map<Class<?>, ObjectCache<?>> cache = Cache.get().getCache();

        for (Class cls : cache.keySet()) {
            System.out.println("# Cached "+cls.getSimpleName()+": " + cache.get(cls).getNumberCached());
        }

        Persister.selectAll(Person.class);

        for (Class cls : cache.keySet()) {
            System.out.println("# Cached "+cls.getSimpleName()+": " + cache.get(cls).getNumberCached());
        }

    }

}
