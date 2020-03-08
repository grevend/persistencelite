package grevend.persistence.lite.extension;

import grevend.persistence.lite.Persistence;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.util.Ignore;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Predicate;

public abstract class Extension {

    private final Persistence persistence;
    private Database database;

    public Extension(@NotNull Persistence persistence) {
        this.persistence = persistence;
    }

    public abstract @NotNull URI getURI() throws URISyntaxException;

    public abstract @NotNull DaoFactory getDaoFactory();

    public @NotNull Predicate<Constructor<?>> isConstructorViable() {
        return constructor -> constructor.getParameterCount() == 0 && !constructor.isSynthetic();
    }

    public @NotNull Predicate<Field> isFieldViable() {
        return field -> !field.isSynthetic()
                && !field.isAnnotationPresent(Ignore.class)
                && !Modifier.isAbstract(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers());
    }

    public Database getDatabase() {
        return database;
    }

    public @NotNull Persistence getPersistence() {
        return persistence;
    }

    public final @NotNull Extension setCredentials(@NotNull String user, @NotNull String password) {
        this.persistence.setCredentials(user, password);
        return this;
    }

    public final @NotNull Database build() throws IllegalStateException {
        this.database = this.persistence.build(this);
        return this.database;
    }

}
