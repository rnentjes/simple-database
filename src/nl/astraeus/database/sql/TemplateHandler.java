package nl.astraeus.database.sql;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;
import nl.astraeus.util.Util;

import java.io.IOException;

/**
 * Date: 11/15/13
 * Time: 9:51 PM
 */
public class TemplateHandler {

    private static TemplateHandler instance = new TemplateHandler();

    public static TemplateHandler get() {
        return instance;
    }

    private SimpleTemplate createTemplate;
    private SimpleTemplate createColumnTemplate;
    private SimpleTemplate insertTemplate;

    public TemplateHandler() {
        try {
            String ct = Util.readAsString(getClass().getResourceAsStream("create.sql"));

            createTemplate = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);

            ct = Util.readAsString(getClass().getResourceAsStream("createColumn.sql"));

            createColumnTemplate = new SimpleTemplate("${", "}", EscapeMode.NONE, ct);
            insertTemplate = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "insert.sql");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public SimpleTemplate getCreateTemplate() {
        return createTemplate;
    }

    public SimpleTemplate getCreateColumnTemplate() {
        return createColumnTemplate;
    }

    public SimpleTemplate getInsertTemplate() {
        return insertTemplate;
    }
}
