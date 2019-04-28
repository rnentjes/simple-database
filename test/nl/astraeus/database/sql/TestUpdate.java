package nl.astraeus.database.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Date: 11/14/13
 * Time: 9:29 PM
 */
public class TestUpdate {

/*
    update ${tableName}
    set ${each(columns as column)}${column} = ?,
    ${eachlast}${column} = ?${/each}
    where ${key} = ?
*/

    @Test
    public void testUpdate() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("def/update.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<String> columns = new ArrayList<>();

        columns.add("address");
        columns.add("age");
        columns.add("zip");
        columns.add("comment");

        model.put("tableName", "person");
        model.put("columns", columns);
        model.put("key", "id");

        Assert.assertEquals("update person\n" +
                "  set address = ?,\n" +
                "      age = ?,\n" +
                "      zip = ?,\n" +
                "      comment = ?\n" +
                "  where id = ?\n", template.render(model));
    }

}
