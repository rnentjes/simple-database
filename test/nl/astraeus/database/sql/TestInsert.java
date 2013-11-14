package nl.astraeus.database.sql;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;

import java.io.IOException;
import java.util.*;

/**
 * Date: 11/14/13
 * Time: 9:29 PM
 */
public class TestInsert {

    public static void main (String [] args) throws IOException {
        TestInsert tct = new TestInsert();

        tct.test();
    }

    public void test() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("insert.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<Map.Entry> columns = new ArrayList<>();

        columns.add(new AbstractMap.SimpleEntry("name", "'John'"));
        columns.add(new AbstractMap.SimpleEntry("age", "31"));

        model.put("tableName", "person");
        model.put("columns", columns);

        System.out.println(template.render(model));
    }

}
