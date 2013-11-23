package nl.astraeus.database;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.jdbc.ConnectionPool;
import nl.astraeus.database.jdbc.ConnectionProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * User: rnentjes
 * Date: 11/23/13
 * Time: 4:44 PM
 */
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

    public static void main(String [] args) {
        // Set the database dialect
        DdlMapping.get().setDatabaseType(DdlMapping.DatabaseDefinition.H2);

        // Set the connection pool provider
        ConnectionPool.get().setConnectionProvider(new ConnectionProvider() {
            @Override
            public Connection getConnection() {
                try {
                    Class.forName("org.h2.Driver");

                    Connection connection = DriverManager.getConnection("jdbc:h2:mem:Example", "sa", "");
                    connection.setAutoCommit(false);

                    return connection;
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                insert(new Person("John", 40, "Somestreet 25"));
                insert(new Person("Jane", 32, "Anotherstreet 54"));
                insert(new Person("Pete", 26, "Roadside 12"));
                insert(new Person("Linda", 10, "Riverside 4"));
            }
        });

        List<Person> persons = Persister.selectAll(Person.class);

        for (Person person : persons) {
            System.out.println("Person name: "+person.getName());
        }
    }
}
