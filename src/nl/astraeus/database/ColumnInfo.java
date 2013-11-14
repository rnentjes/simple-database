package nl.astraeus.database;

/**
 * Date: 11/14/13
 * Time: 9:02 PM
 */
public class ColumnInfo {

    private String name;
    private String type;

    public ColumnInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}
