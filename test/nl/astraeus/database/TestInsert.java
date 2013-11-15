package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestInsert {

    public static void main(String [] args) {
        Person person = new Person(1L, "Rien", 40, "Rozendael");

        Persister.begin();

        Persister.insert(person);

        Persister.commit();
    }

}
