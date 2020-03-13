package grevend.persistence.lite;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.database.DatabaseBuilder;
import org.jetbrains.annotations.NotNull;

public final class Persistence {

  public static @NotNull <D extends Database> DatabaseBuilder<D> databaseBuilder(
      Class<D> databaseImplementation,
      @NotNull String name, int version) {
    return new DatabaseBuilder<>(databaseImplementation).setNameAndVersion(name, version);
  }

}
