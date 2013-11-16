package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestFind {

    public static void main(String [] args) {
        Person person = new Person("Rien", 40, "Rozendael");

        Persister.begin();

        Persister.insert(person);
        Long id = person.getId();

        Persister.commit();

        Person p2 = Persister.find(Person.class, id);

        System.out.println("p2.name: "+p2.getName());
    }

}
