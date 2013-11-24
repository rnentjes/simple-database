package nl.astraeus.database.sql;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;
import org.junit.Ignore;

import java.io.IOException;
import java.util.*;

/**
 * Date: 11/14/13
 * Time: 9:29 PM
 */
@Ignore
public class TestUpdate {

    public static void main (String [] args) throws IOException {
        TestUpdate tct = new TestUpdate();

        tct.test();
    }

    public void test() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("update.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<String> columns = new ArrayList<>();

        columns.add("address");
        columns.add("age");
        columns.add("zip");
        columns.add("comment");

        List<String> keys = new ArrayList<>();

        keys.add("name");
        keys.add("id");

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
