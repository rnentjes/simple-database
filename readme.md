# Simple database

## Maven, gradle etc:

Maven repository: https://nexus.astraeus.nl/nexus/content/groups/public

Pom:

```xml
<dependency>
  <groupId>nl.astraeus</groupId>
  <artifactId>simple-database</artifactId>
  <version>2.0.1</version>
</dependency>
```

## Minimal example:

```java
@Table(name="persons")
@Cache(maxSize = 1000)
public class Person {

    @Id
    private Long id;

    @Length(value = 200)
    @Default("'new name'")
    private String name;
    @Default("21")
    private int age;
    @Length(precision = 10, scale = 2)
    private double balance;
    private String address;

    // needed for retrieval from db
    public Person() {}

    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }
    
    // getter & setters etc.
}

// see in test: nl.astraeus.database.example.MinimalExample
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
```
