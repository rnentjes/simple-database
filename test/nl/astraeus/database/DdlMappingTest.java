package nl.astraeus.database;

import nl.astraeus.template.SimpleTemplate;

import org.junit.Assert;
import org.junit.Test;

/**
 * Date: 11/19/13
 * Time: 11:07 PM
 */
public class DdlMappingTest {

    @Test
    public void testDdlMapping() {
        DdlMapping ddlMapping = new DdlMapping(DdlMapping.DatabaseDefinition.H2);

        SimpleTemplate template = ddlMapping.getDdlTemplateForType(Double.class);

        Assert.assertEquals(2, template.getUsedParamaterNames().size());
    }
}
