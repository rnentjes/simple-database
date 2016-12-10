package nl.astraeus.database;

import org.h2.util.New;
import org.junit.Test;

/**
 * Date: 11/19/13
 * Time: 11:07 PM
 */
public class DdlMappingTest {

    @Test
    public void getInstance() {
        DdlMapping ddlMapping = new DdlMapping(DdlMapping.DatabaseDefinition.H2);

        ddlMapping.getDdlTemplateForType(Long.class);
    }
}
