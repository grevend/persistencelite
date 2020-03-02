package grevend.persistence.lite;

import grevend.persistence.lite.database.Database;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Persistence {

    private final String name;
    private final int version;
    private String user = null, password = null;

    private Persistence(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Persistence databaseBuilder(String name, int version) {
        return new Persistence(name, version);
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
        return new Database(Database.SQL, "jdbc:postgresql://localhost/", this.name, this.user, this.password,
                this.version);
    }

}
