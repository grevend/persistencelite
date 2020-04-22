/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.main;

import grevend.persistencelite.PersistenceLite;
import grevend.persistencelite.entity.Entity;
import grevend.persistencelite.entity.Id;
import grevend.persistencelite.entity.Property;
import grevend.persistencelite.service.sql.PostgresService;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Throwable {
        PostgresService service = new PostgresService();
        var dao = service.createDao(Customer.class);
        var customer = new Customer(21, "Justin", "987654321", "justin@van.com", "Van...", 21);
        dao.create(new Customer(12, "Bob", "123456789", "bob@van.com", "Van...", 12));
        dao.create(customer);
        System.out.println(dao.retrieve());
        System.out.println(dao.retrieve(Map.of("id", 12, "username", "Bob")));
        System.out.println(dao.retrieve(Map.of("id", 13, "username", "Bob")));
        dao.delete(customer);
        System.out.println(dao.retrieve());
        var bob = dao.retrieve(Map.of("id", 12, "username", "Bob"));
        if (bob.isPresent()) {
            dao.update(bob.get(), Map.of("password", "Hello World!"));
        }
        System.out.println(dao.retrieve());

        /*var service = PersistenceLite.configureService(PostgresService.class)
            .loadCredentials("credentials.properties").service();*/
    }

    @Entity(name = "account_base")
    public interface AccountBase {

        @Id
        int id();

        @Id
        String username();
    }

    @Entity(name = "account2")
    public interface Account extends AccountBase {

        @Id
        int id();

        @Id
        String username();

        String password();
    }

    @Entity(name = "customer")
    public record Customer(@Id int id, @Id String username, String password, String email, @Property(name = "company_name")String companyName,
                           @Property(name = "account_id")int accountId) implements Account {}

}
