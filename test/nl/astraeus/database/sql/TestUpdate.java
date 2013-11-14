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
public class TestUpdate {

    public static void main (String [] args) throws IOException {
        TestUpdate tct = new TestUpdate();

        tct.test();
    }

    public void test() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("update.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<Map.Entry> columns = new ArrayList<>();

        columns.add(new AbstractMap.SimpleEntry("address", "'Some street 1'"));
        columns.add(new AbstractMap.SimpleEntry("age", "31"));
        columns.add(new AbstractMap.SimpleEntry("zip", "1234"));
        columns.add(new AbstractMap.SimpleEntry("comment", "'Some Pipo somewhere in clownsville'"));

        List<Map.Entry> keys = new ArrayList<>();

        keys.add(new AbstractMap.SimpleEntry("name", "'John'"));
        keys.add(new AbstractMap.SimpleEntry("id", "12345"));

        model.put("tableName", "person");
        model.put("columns", columns);
        model.put("keys", keys);

        String sql;

        for (int i = 0; i < 100000; i++) {
            sql = template.render(model);
        }

        long start = System.nanoTime();
        sql = template.render(model);
        long end = System.nanoTime();

        System.out.println(sql);
        System.out.println((end-start)+"ns");
    }

}
