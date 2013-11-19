package nl.astraeus.database;

import org.junit.Test;

/**
 * Date: 11/19/13
 * Time: 11:07 PM
 */
public class DdlMappingTest {

    @Test
    public void getInstance() {
        DdlMapping.get().getDdlTemplateForType(Long.class);
    }
}
