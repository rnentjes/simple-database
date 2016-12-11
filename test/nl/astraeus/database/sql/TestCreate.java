package nl.astraeus.database.sql;

import nl.astraeus.database.ColumnInfo;
import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 9:29 PM
 */
public class TestCreate {

/*
    create table ${tableName} (${key} BIGINT AUTO_INCREMENT,${each(columns as column)}
    ${column.name} ${column.type},${/each}
    primary key(${key})
            )
*/


    @Test
    public void testCreate() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("def/create.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<ColumnInfo> columns = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        columns.add(new ColumnInfo("name", "VARCHAR(255)"));
        columns.add(new ColumnInfo("age", "SMALLINT"));

        keys.add("name");

        model.put("tableName", "person");
        model.put("key", "name");
        model.put("columns", columns);
        model.put("keys", keys);

        Assert.assertEquals("create table person (name BIGINT AUTO_INCREMENT,\n" +
                "    name VARCHAR(255),\n" +
                "    age SMALLINT,\n" +
                "    primary key(name)\n" +
                ")\n", template.render(model));
    }
}
