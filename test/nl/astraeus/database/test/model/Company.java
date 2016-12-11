package nl.astraeus.database.test.model;

import java.util.ArrayList;
import java.util.List;

import nl.astraeus.database.annotations.Collection;
import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;

import org.junit.Ignore;

/**
 * User: rnentjes
 * Date: 11/13/13
 * Time: 4:10 PM
 */
@Ignore
@Table
public class Company {

    @Id
    private Long id;

    private String name;

    @Collection(Info.class)
    private List<Info> infoLines = new ArrayList<Info>();

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

    public void addInfo(Info info) {
        infoLines.add(info);
    }

    public List<Info> getInfoLines() {
        return infoLines;
    }
}
