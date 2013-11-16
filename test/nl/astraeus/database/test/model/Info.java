package nl.astraeus.database.test.model;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;

/**
 * Date: 11/16/13
 * Time: 9:43 PM
 */
@Table
public class Info {

    @Id
    private Long id;

    private String description;
    private String info;

    public Info() {
    }

    public Info(String description, String info) {
        this.description = description;
        this.info = info;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

