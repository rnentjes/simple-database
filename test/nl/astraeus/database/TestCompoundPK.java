package nl.astraeus.database;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Table;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 11/17/13
 * Time: 12:58 PM
 */
public class TestCompoundPK extends BaseTest {
    private final static Logger logger = LoggerFactory.getLogger(TestCompoundPK.class);

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestCompoundPK");
    }

    @Table(name="test")
    public static class CompoundError {
        @Id
        Long id1;

        @Id
        Long id2;
    }

    @Table(name="test2")
    public static class InvalidPrimaryKeyType {
        @Id
        String name;

        Long id2;
        String data;
    }

    @Test(expected = IllegalStateException.class)
    public void testCompoundError() {
        SimpleDao<CompoundError> dao = new SimpleDao<>(CompoundError.class);

        dao.insert(new CompoundError());
    }


    @Test(expected = IllegalStateException.class)
    public void testInvalidPrimaryKey() {
        SimpleDao<InvalidPrimaryKeyType> dao = new SimpleDao<>(InvalidPrimaryKeyType.class);

        dao.insert(new InvalidPrimaryKeyType());
    }
}
