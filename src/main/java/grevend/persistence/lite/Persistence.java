package grevend.persistence.lite;

import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.sql.postgresql.PostgresqlDaoFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Persistence {

    private final String name;
    private final int version;
    private final DaoFactory factory;
    private String user = null, password = null;

    private Persistence(String name, int version, DaoFactory factory) {
        this.name = name;
        this.version = version;
        this.factory = factory;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull Persistence databaseBuilder(String name, int version, DaoFactory factory) {
        return new Persistence(name, version, factory);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Persistence databaseBuilder(String name, int version) {
        return databaseBuilder(name, version, PostgresqlDaoFactory.getInstance());
    }

    public @NotNull Persistence setCredentials(@NotNull String user, @NotNull String password) {
        this.user = user;
        this.password = password;
        return this;
    }

    public @NotNull Database build() throws IllegalStateException {
        if (this.user == null || this.password == null) {
            throw new IllegalStateException("Credentials must be set before building the database.");
        }
        return new Database(factory, Database.SQL, "jdbc:postgresql://localhost/",
                this.name, this.user, this.password, this.version);
    }

}
