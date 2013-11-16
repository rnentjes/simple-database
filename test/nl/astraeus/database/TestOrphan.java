package nl.astraeus.database;

import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Person;

/**
 * Date: 11/16/13
 * Time: 12:27 AM
 */
public class TestOrphan {

    public static void main(String [] args) {
        final Person person = new Person("Test", 44, "Somewhere");
        final Company company = new Company("Some company");

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                person.setCompany(company);

                insert(person);
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                delete(company);
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                Person found = find(Person.class, person.getId());

                System.out.println("Found: "+found.getName());
                System.out.println("Company: "+found.getCompany());
            }
        });

        Persister.execute(new Persister.Executor() {
            @Override
            public void execute() {
                delete(person);
            }
        });
    }

}
