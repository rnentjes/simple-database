package nl.astraeus.database;

import nl.astraeus.database.test.model.Person;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestInsert {

    public static void main(String [] args) {
        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                insert(new Person("Rien", 40, "Rozendael"));
                insert(new Person("Jan", 32, "Straat"));
                insert(new Person("Piet", 26, "Weg"));
                insert(new Person("Klaas", 10, "Pad"));
            }
        });
    }

}
