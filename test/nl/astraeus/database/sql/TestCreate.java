package nl.astraeus.database.sql;

import nl.astraeus.database.ColumnInfo;
import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;
import org.junit.Ignore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/14/13
 * Time: 9:29 PM
 */
@Ignore
public class TestCreate {

    public static void main (String [] args) throws IOException {
        TestCreate tct = new TestCreate();

        tct.test();
    }

    public void test() throws IOException {
        String ct = Util.readAsString(getClass().getResourceAsStream("create.sql"));

        SimpleTemplate template = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

        Map<String, Object> model = new HashMap<>();

        List<ColumnInfo> columns = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        columns.add(new ColumnInfo("name", "VARCHAR(255)"));
        columns.add(new ColumnInfo("age", "SMALLINT"));

        keys.add("name");

        model.put("tableName", "person");
        model.put("columns", columns);
        model.put("keys", keys);

        System.out.println(template.render(model));

    }
}
