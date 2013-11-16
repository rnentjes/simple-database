package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:10 PM
 */
@Table
public class Company {

    @Id
    private Long id;

    private String name;

    //@Collection(Person.class)
    //private List<Person> employees;

    public Company() {}

    public Company(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
