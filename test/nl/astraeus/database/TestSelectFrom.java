package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;
import org.junit.Ignore;

import java.util.List;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
@Ignore
public class TestSelectFrom {

    public static void main(String [] args) {
/*
        DdlMapping.get().setExecuteDDLUpdates(true);

        Persister.begin();

        Persister.insert(new Person("Rien", 40, "Rozendael"));
        Persister.insert(new Person("Jan", 32, "Straat"));
        Persister.insert(new Person("Ronald", 32, "Wherever"));
        Persister.insert(new Person("Piet", 26, "Weg"));
        Persister.insert(new Person("Klaas", 10, "Pad"));

        Persister.commit();

        List<Person> persons = Persister.selectFrom(Person.class, "where age > ?", 30);

        for (Person person : persons) {
            System.out.println("age > 30 Found: "+person.getName());
        }

        persons = Persister.selectWhere(Person.class, "name like ?", "R%");

        for (Person person : persons) {
            System.out.println("name = 'R%' Found: "+person.getName());
        }
*/
    }

}
