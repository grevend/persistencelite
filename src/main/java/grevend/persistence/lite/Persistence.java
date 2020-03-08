package grevend.persistence.lite;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.extension.Extension;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class Persistence {

    private static Persistence instance;

    private String name, user, password;
    private int version;

    private Persistence() {
    }

    private static Persistence getInstance() {
        if (instance == null) {
            instance = new Persistence();
        }
        return instance;
    }

    public static @NotNull <E extends Extension> E databaseBuilder(@NotNull Class<E> extension, @NotNull String name,
                                                                   int version)
            throws IllegalArgumentException {
        getInstance().name = name;
        getInstance().version = version;
        try {
            Constructor<E> constructor = extension.getConstructor(Persistence.class);
            return constructor.newInstance(getInstance());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            getInstance().name = null;
            getInstance().version = 0;
            throw new IllegalArgumentException(extension.getCanonicalName() +
                    " does not provide a public constructor with one parameter of type " +
                    Persistence.class.getCanonicalName() + ".", e);
        }
    }

    public @NotNull void setCredentials(@NotNull String user, @NotNull String password) {
        this.user = user;
        this.password = password;
    }

    public @NotNull Database build(@NotNull Extension extension) throws IllegalStateException {
        if (this.user == null || this.password == null) {
            throw new IllegalStateException("Credentials must be set before building the database.");
        }
        return new Database(extension, this.name, this.version, this.user, this.password);
    }

}
