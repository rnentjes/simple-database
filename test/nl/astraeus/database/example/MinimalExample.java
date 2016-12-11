package nl.astraeus.database.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import nl.astraeus.database.SimpleDao;
import nl.astraeus.database.SimpleDatabase;
import nl.astraeus.database.jdbc.ConnectionProvider;
import nl.astraeus.database.test.model.Person;

/**
 * User: rnentjes
 * Date: 11-12-16
 * Time: 16:26
 */
class MinimalExample {

    public static void main(String[] args) {
        // define the default database, all it needs it a way to get a connection
        SimpleDatabase db = SimpleDatabase.define(new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException, ClassNotFoundException {
                Class.forName("org.h2.Driver");

                Connection connection = DriverManager.getConnection("jdbc:h2:mem:Example;DB_CLOSE_DELAY=-1", "sa", "");
                connection.setAutoCommit(false);

                return connection;
            }
        });

        // automatically create database tables and columns if needed
        db.setExecuteDDLUpdates(true);

        // use default dao (extends it if you need more)
        SimpleDao<Person> personDao = new SimpleDao<>(Person.class);

        // execute multiple dao actions in transaction
        personDao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                dao.insert(new Person("John", 40, "Road"));
                dao.insert(new Person("Jan", 32, "Straat"));
                dao.insert(new Person("Ronald", 31, "Wherever"));
                dao.insert(new Person("Piet", 26, "Weg"));
                dao.insert(new Person("Klaas", 10, "Pad"));
            }
        });

        // find persons, read actions don't need a transaction
        List<Person> persons = personDao.where("name like ?", "J%");

        for (Person person : persons) {
            System.out.println("Person: " + person.getName());
        }

        try {
            // start transaction because of the update
            db.begin();

            Person person = personDao.find("name = ?", "John");

            person.setName("Johnny");

            personDao.update(person);

            db.commit();
        } finally {
            // transaction should be closed by commit
            if (db.transactionActive()) {
                // otherwise something went wrong
                db.rollback();
            }
        }

        persons = personDao.where("name like ?", "J%");

        for (Person person : persons) {
            System.out.println("Person: " + person.getName());
        }
    }
}