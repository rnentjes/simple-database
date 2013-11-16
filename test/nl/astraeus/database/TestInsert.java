package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestInsert {

    public static void main(String [] args) {
        Persister.begin();

        Persister.insert(new Person("Rien", 40, "Rozendael"));
        Persister.insert(new Person("Jan", 32, "Straat"));
        Persister.insert(new Person("Piet", 26, "Weg"));
        Persister.insert(new Person("Klaas", 10, "Pad"));

        Persister.commit();
    }

}
