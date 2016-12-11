package nl.astraeus.database.sql;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Date: 11/14/13
 * Time: 9:29 PM
 */
public class TestInsert {

    @Test
    public void testInsert() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("def/insert.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<String> columns = new ArrayList<>();

        columns.add("name");
        columns.add("age");

        model.put("tableName", "person");
        model.put("columns", columns);

        Assert.assertEquals("insert into person\n" +
                "  (name, age) values\n" +
                "  (?, ?)\n", template.render(model));
    }

}
