package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Table;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:09 PM
 */
@Persistent
@Table(name="persons")
public class Person {

    @Length(200)
    @Default("'new name'")
    private String name;
    @Default("21")
    private int age;
    private Company company;


}
