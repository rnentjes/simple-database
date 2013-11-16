package nl.astraeus.database;

import nl.astraeus.database.cache.Cache;
import nl.astraeus.database.cache.ObjectCache;
import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Person;

import java.util.Map;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestReference {

    public static void main(String [] args) {
        Persister.begin();

        Person person = new Person("Rien", 40, "Rozendael");
        Company company = new Company("Astraeus BV");
        person.setCompany(company);

        Persister.insert(company);
        Persister.insert(person);

        Persister.commit();

        Person p2 = Persister.find(Person.class, person.getId());

        System.out.println("p2 company "+p2.getCompany());
        System.out.println("p2 company name "+p2.getCompany().getName());

        Map<Class<?>, ObjectCache<?>> cache = Cache.get().getCache();

        for (Class cls : cache.keySet()) {
            System.out.println("# Cached "+cls.getSimpleName()+": " + cache.get(cls).getNumberCached());
        }
    }

}
