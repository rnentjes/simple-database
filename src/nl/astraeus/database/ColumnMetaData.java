package nl.astraeus.database;

/**
 * User: rnentjes
 * Date: 10-12-16
 * Time: 13:27
 */
public class ColumnMetaData {
    private String name;
    private Integer sqlType;
    private Integer size;
    private Integer precision;

    public ColumnMetaData(String name, Integer sqlType, Integer size, Integer precision) {
        this.name = name;
        this.sqlType = sqlType;
        this.size = size;
        this.precision = precision;
    }

    public String getName() {
        return name;
    }

    public Integer getSqlType() {
        return sqlType;
    }

    public Integer getSize() {
        return size;
    }

    public Integer getPrecision() {
        return precision;
    }
}
