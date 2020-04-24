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

import grevend.persistencelite.entity.Entity;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.entity.EntityProperty;
import grevend.persistencelite.entity.Id;
import grevend.persistencelite.entity.Property;
import grevend.persistencelite.entity.Relation;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        System.out.println(
            List.of(DiscordCredential.class, Author.class, Pet.class, Dog.class, Cat.class,
                Book.class).stream()
                .map(EntityMetadata::of)
                .map(EntityMetadata::inferRelationTypes)
                .flatMap(entityMetadata -> entityMetadata.getDeclaredRelations().stream())
                .map(EntityProperty::relation)
                .map(Objects::toString)
                .collect(Collectors.joining(System.lineSeparator())));
    }

    enum Status {
        ALIVE, DEAD;
    }

    @Entity(name = "pet")
    public interface Pet {

        @Id
        int id();

        @Property(name = "status"/*, autoGenerated = true*/)
        Status status();

        String name();

        @Property(name = "owner_id")
        int ownerId();
    }

    @Entity(name = "discord_credential")
    public record DiscordCredential(
        @Id @Property(name = "display_name")
        String displayName,
        @Id @Property(name = "tag_number")
        String tagNumber,
        String password,
        @Relation(selfProperties = {"display_name",
            "tag_number"}, targetEntity = Author.class, targetProperties = {"discord_name",
            "discord_tag"})
        Author author
    ) {}

    @Entity(name = "author")
    public record Author(
        @Id(autoGenerated = true)
        int id,
        @Property(name = "first_name")
        String firstName,
        @Property(name = "middle_name")
        String middleName,
        @Property(name = "last_name")
        String lastName,
        @Property(name = "date_of_birth")
        LocalDateTime dateOfBirth,
        @Property(name = "discord_name")
        String nameType,
        @Property(name = "discord_tag")
        String tagType,
        @Relation(selfProperties = "id", targetEntity = Pet.class, targetProperties = "owner_id")
        Collection<Pet>pets,
        @Relation(selfProperties = {"discord_name",
            "discord_tag"}, targetEntity = DiscordCredential.class, targetProperties = {
            "display_name", "tag_number"})
        DiscordCredential discordCredential,
        @Relation(selfProperties = {}, targetEntity = Book.class, targetProperties = {})
        Collection<Book>books
    ) {}

    @Entity(name = "dog")
    public record Dog(
        @Id
        int id,
        String name,
        Status status,
        int ownerId,
        @Property(name = "trained"/*, autoGenerated = true*/)
        boolean trained
    ) implements Pet {}

    @Entity(name = "cat")
    public record Cat(
        @Id
        int id,
        String name,
        Status status,
        int ownerId,
        @Property(name = "destroy_stuff"/*, autoGenerated = true*/)
        boolean destroyStuff
    ) implements Pet {}

    @Entity(name = "book")
    public record Book(
        @Id
        String isbn,
        String title,
        @Relation(selfProperties = {}, targetEntity = Author.class, targetProperties = {})
        Collection<Author>authors
    ) {}

    /*public static void main(String[] args) throws Throwable {
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

        var service = PersistenceLite.configureService(PostgresService.class)
            .loadCredentials("credentials.properties").service();
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
                           @Property(name = "account_id")int accountId) implements Account {}*/

}
