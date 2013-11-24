package nl.astraeus.database.test.metadata;

import nl.astraeus.database.MetaData;
import nl.astraeus.database.MetaDataHandler;
import nl.astraeus.database.Persister;
import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Person;
import org.junit.Ignore;

/**
 * Date: 11/13/13
 * Time: 10:18 PM
 */
@Ignore
public class TestMetaData {

    public static void main(String [] args) {
        Persister.begin();

        MetaDataHandler.get().getMetaData(Company.class);

        MetaData metaData = new MetaData(Person.class);

        System.out.println(metaData);

        Persister.commit();
    }
}
