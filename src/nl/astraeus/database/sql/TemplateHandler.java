package nl.astraeus.database.sql;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;

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
    private SimpleTemplate updateTemplate;
    private SimpleTemplate deleteTemplate;
    private SimpleTemplate selectTemplate;
    private SimpleTemplate selectWhereTemplate;

    public TemplateHandler() {
        createTemplate          = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "create.sql");
        createColumnTemplate    = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "createColumn.sql");
        insertTemplate          = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "insert.sql");
        updateTemplate          = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "update.sql");
        deleteTemplate          = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "delete.sql");
        selectTemplate          = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "select.sql");
        selectWhereTemplate     = new SimpleTemplate("${", "}", EscapeMode.NONE, getClass(), "selectWhere.sql");
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

    public SimpleTemplate getSelectWhereTemplate() {
        return selectWhereTemplate;
    }

    public SimpleTemplate getSelectTemplate() {
        return selectTemplate;
    }

    public SimpleTemplate getUpdateTemplate() {
        return updateTemplate;
    }

    public SimpleTemplate getDeleteTemplate() {
        return deleteTemplate;
    }
}
