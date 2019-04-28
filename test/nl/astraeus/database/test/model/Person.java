package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Cache;
import nl.astraeus.database.annotations.Clob;
import nl.astraeus.database.annotations.Default;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Table;
import org.junit.Ignore;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:09 PM
 */
@Ignore
@Table(name="persons")
@Cache(maxSize = 6)
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

    private Company company;

    @Clob
    private String newColumnTest;

    // needed for retrieval from db
    public Person() {}

    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;


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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getNewColumnTest() {
        return newColumnTest;
    }

    public void setNewColumnTest(String newColumnTest) {
        this.newColumnTest = newColumnTest;
    }
}
