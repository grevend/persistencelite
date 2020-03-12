package grevend.persistence.lite.database;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

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

  public @NotNull D build() {
    if (this.name == null) {
      throw new DatabaseBuilderException(
          "Name and version must be set before building the database.");
    }
    if (this.user == null || this.password == null) {
      throw new DatabaseBuilderException("Credentials must be set before building the database.");
    }
    try {
      Constructor<D> constructor = this.databaseImplementation
          .getConstructor(String.class, Integer.TYPE, String.class, String.class);
      return constructor.newInstance(this.name, this.version, this.user, this.password);
    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      throw new DatabaseBuilderException("Database construction failed.", e);
    }
  }

}
