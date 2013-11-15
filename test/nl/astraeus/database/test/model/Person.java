package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Default;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Table;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:09 PM
 */
@Table(name="persons")
public class Person {

    @Id
    private Long id;

    @Length(value = 200)
    @Default("'new name'")
    private String name;
    @Default("21")
    private int age;
    private String address;
    private Company company;

    public Person(Long id, String name, int age, String address) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
    }

}
