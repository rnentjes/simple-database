package nl.astraeus.database;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import nl.astraeus.database.annotations.Id;
import nl.astraeus.database.annotations.Length;
import nl.astraeus.database.annotations.Serialized;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestDataTypes extends BaseTest {

    @BeforeClass
    public static void createDatabase() {
        BaseTest.createDatabase("jdbc:h2:mem:TestDataTypes");
    }

    public static class DataTypes {

        @Id
        public Long id;

        @Length(199)
        public String name;

        @Length(precision = 10, scale = 4)
        public double amount;

        @Length(precision = 12, scale = 6)
        public BigDecimal price;

        public int age;
        public long created;

        public Date lastAccess;

        public boolean bit;

        @Serialized
        public String blub;

    }

    @Test
    public void testCache() {
        SimpleDao<DataTypes> dao = new SimpleDao<DataTypes>(DataTypes.class);

        dao.execute(new SimpleDao.Executor<DataTypes>() {
            @Override
            public void execute(SimpleDao<DataTypes> dao) {
                DataTypes type = new DataTypes();

                type.name = "name";
                type.amount = 6.66;
                type.price = new BigDecimal("7.77");
                type.age = 33;
                type.created = System.currentTimeMillis();
                type.lastAccess = new Date();
                type.blub = "Some serialized string thingy!";
                type.bit = true;

                dao.insert(type);

                List<DataTypes> types = dao.all();

                type = types.get(0);

                System.out.println("Name: "+type.name);
                System.out.println("Amount: "+type.amount);
                System.out.println("Price: "+type.price);
                System.out.println("Age: "+type.age);
                System.out.println("Created: "+type.created+" ("+new Date(type.created)+")");
                System.out.println("LastAccess: "+type.lastAccess);
                System.out.println("Blub: "+type.blub);
                System.out.println("Bit: "+type.bit);

                db.getCache().clear();

                types = dao.all();

                type = types.get(0);

                System.out.println("Name: "+type.name);
                System.out.println("Amount: "+type.amount);
                System.out.println("Price: "+type.price);
                System.out.println("Age: "+type.age);
                System.out.println("Created: "+type.created+" ("+new Date(type.created)+")");
                System.out.println("LastAccess: "+type.lastAccess);
                System.out.println("Blub: "+type.blub);
                System.out.println("Bit: "+type.bit);
            }
        });
    }


}
