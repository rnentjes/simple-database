package nl.astraeus.database;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;
import org.junit.Ignore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * User: rnentjes
 * Date: 11/23/13
 * Time: 4:44 PM
 */
@Ignore
public class Example {
    public static class Person {
        @Id
        private Long id;

        private String name;
        private int age;
        private String address;

        // needed for SimpleDatabase
        public Person() {}

        public Person(String name, int age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        // get for id
        // getter and setters for name, age and address

        public String getName() {
            return name;
        }
    }

    public static void main(String [] args) throws InterruptedException {

        SimpleDatabase db = SimpleDatabase.define(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("nl.astraeus.jdbc.Driver");
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:stat::jdbc:h2:mem:Example", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException | SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        db.setExecuteDDLUpdates(true);

        SimpleDao<Person> dao = new SimpleDao<Person>(Person.class);

        dao.execute(new SimpleDao.Executor<Person>() {
            @Override
            public void execute(SimpleDao<Person> dao) {
                dao.insert(new Person("John", 40, "Somestreet 25"));
                dao.insert(new Person("Jane", 32, "Anotherstreet 54"));
                dao.insert(new Person("Pete", 26, "Roadside 12"));
                dao.insert(new Person("Linda", 10, "Riverside 4"));
            }
        });

        List<Person> persons = dao.selectAll();

        for (Person person : persons) {
            System.out.println("Person name: "+person.getName());
        }
    }
}
