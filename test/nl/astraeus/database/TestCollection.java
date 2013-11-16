package nl.astraeus.database;

import nl.astraeus.database.test.model.Company;
import nl.astraeus.database.test.model.Info;

/**
 * Date: 11/16/13
 * Time: 9:45 PM
 */
public class TestCollection {


    public static void main(String [] args) {
        Persister.begin();

        Company company = new Company("Astraeus BV");

        company.addInfo(new Info("description 1", "info 1"));
        company.addInfo(new Info("description 2", "info 2"));

        Persister.insert(company);

        Persister.commit();

        Company found = Persister.find(Company.class, company.getId());

        System.out.println("company "+company.getName());

        for (Info info : company.getInfoLines()) {
            System.out.println("info: "+info.getInfo()+" - "+info.getDescription());
         }

        Persister.begin();

        company.addInfo(new Info("description 3", "info 3"));

        Persister.update(company);

        Persister.commit();

        found = Persister.find(Company.class, company.getId());

        System.out.println("company "+company.getName());

        for (Info info : company.getInfoLines()) {
            System.out.println("info: "+info.getInfo()+" - "+info.getDescription());
        }

        Persister.begin();

        Info infox = company.getInfoLines().remove(1);

        Persister.delete(infox);
        Persister.update(company);

        Persister.commit();

        found = Persister.find(Company.class, company.getId());

        System.out.println("company "+company.getName());

        for (Info info : company.getInfoLines()) {
            System.out.println("info: "+info.getInfo()+" - "+info.getDescription());
        }

        for (Info info : Persister.selectAll(Info.class)) {
            System.out.println("--> Info: "+info.getInfo()+" - "+info.getDescription());
        }
    }

}
