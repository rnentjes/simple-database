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
public class TestSelect {

    @Test
    public void testSelect() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("def/select.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        model.put("tableName", "person");
        model.put("key", "id");

        Assert.assertEquals("select \n" +
                "  from person\n" +
                "  where id = ?\n", template.render(model));
    }

}
