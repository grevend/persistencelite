package grevend.persistence.lite.database;

import org.jetbrains.annotations.NotNull;

public abstract class DatabaseBuilder<D extends Database> {

  protected final String name;
  protected final int version;

  protected DatabaseBuilder(@NotNull String name, int version) {
    this.name = name;
    this.version = version;
  }

  public abstract @NotNull D build();

}
