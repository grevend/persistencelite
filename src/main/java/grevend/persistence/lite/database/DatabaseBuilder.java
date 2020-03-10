package grevend.persistence.lite.database;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DatabaseBuilder<D extends Database> {

    private final Class<D> databaseImplementation;
    private String name, user, password;
    private int version;

    public DatabaseBuilder(@NotNull Class<D> databaseImplementation) {
        this.databaseImplementation = databaseImplementation;
    }

    public final DatabaseBuilder<D> setNameAndVersion(@NotNull String name, int version) {
        this.name = name;
        this.version = version;
        return this;
    }

    public final DatabaseBuilder<D> setCredentials(@NotNull String user, @NotNull String password) {
        this.user = user;
        this.password = password;
        return this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull D build()
            throws IllegalStateException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        if (this.name == null) {
            throw new IllegalStateException("Name and version must be set before building the database.");
        }
        if (this.user == null || this.password == null) {
            throw new IllegalStateException("Credentials must be set before building the database.");
        }
        Constructor<D> constructor =
                databaseImplementation.getConstructor(String.class, Integer.TYPE, String.class, String.class);
        if (constructor != null) {
            return constructor.newInstance(this.name, this.version, this.user, this.password);
        } else {
            throw new IllegalStateException("No constructor found in Database implementation.");
        }
    }

}
