package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;

import java.util.List;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestDelete {

    public static void main(String [] args) {
        Persister.begin();

        Persister.insert(new Person("Rien", 40, "Rozendael"));
        Persister.insert(new Person("Jan", 32, "Straat"));
        Persister.insert(new Person("Piet", 26, "Weg"));
        Persister.insert(new Person("Klaas", 10, "Pad"));

        Persister.commit();


        List<Person> persons = Persister.selectWhere(Person.class, "age > ?", 30);

        Persister.begin();

        for (Person person : persons) {
            System.out.println("age > 30 Found: "+person.getName()+" age: "+person.getAge());

            Persister.delete(person);
        }

        Persister.commit();

        persons = Persister.selectAll(Person.class);

        for (Person person : persons) {
            System.out.println("2: age > 30 deleted Found: "+person.getName()+" age: "+person.getAge());
        }

    }

}