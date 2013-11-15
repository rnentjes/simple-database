package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Collection;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Table;

import java.util.List;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:10 PM
 */
@Table
public class Company {

    @Id
    private int id;

    @Length(precision = 12, scale = 4)
    private double amount;

    @Collection(Person.class)
    private List<Person> employees;

}
