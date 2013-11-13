package nl.astraeus.database.test.metadata;

import nl.astraeus.database.MetaData;
import nl.astraeus.database.test.model.Person;

/**
 * Date: 11/13/13
 * Time: 10:18 PM
 */
public class TestMetaData {

    public static void main(String [] args) {
        MetaData metaData = new MetaData(Person.class);

        System.out.println(metaData);
    }
}
