package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Length;

import java.util.List;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:10 PM
 */
public class Company {

    @Length(precision = 12, scale = 4)
    private double amount;
    private List<Person> employees;

}
