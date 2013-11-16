package nl.astraeus.database;

import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.cache.ObjectCache;
import nl.astraeus.database.test.model.Person;

import java.util.List;
import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestSelectAll {

    public static void main(String [] args) {
        Persister.begin();

        Persister.insert(new Person("Rien", 40, "Rozendael"));
        Persister.insert(new Person("Jan", 32, "Straat"));
        Persister.insert(new Person("Ronald", 32, "Wherever"));
        Persister.insert(new Person("Piet", 26, "Weg"));
        Persister.insert(new Person("Klaas", 10, "Pad"));

        Persister.commit();

        List<Person> persons = Persister.selectAll(Person.class);
        Cache.get().clear();

        long start1 = System.nanoTime();
        persons = Persister.selectAll(Person.class);
        long stop1 = System.nanoTime();

        for (Person person : persons) {
            System.out.println("all Found: "+person.getName());
        }

        long start2 = System.nanoTime();
        persons = Persister.selectAll(Person.class);
        long stop2 = System.nanoTime();

        for (Person person : persons) {
            System.out.println("2all Found: "+person.getName());
        }

        long start3 = System.nanoTime();
        persons = Persister.selectAll(Person.class);
        long stop3 = System.nanoTime();

        for (Person person : persons) {
            System.out.println("3all Found: "+person.getName());
        }

        System.out.println("time1 "+(stop1-start1));
        System.out.println("time2 "+(stop2-start2));
        System.out.println("time3 "+(stop3-start3));

        Map<Class<?>, ObjectCache<?>> cache = Cache.get().getCache();

        for (Class cls : cache.keySet()) {
            System.out.println("# Cached "+cls.getSimpleName()+": " + cache.get(cls).getNumberCached());
        }
    }

}
