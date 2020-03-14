package grevend.persistence.lite.database.inmemory;

import grevend.persistence.lite.database.DatabaseBuilder;
import org.jetbrains.annotations.NotNull;

public class InMemoryDatabaseBuilder extends DatabaseBuilder<InMemoryDatabase> {

  public InMemoryDatabaseBuilder(@NotNull String name, int version) {
    super(name, version);
  }

  @NotNull
  @Override
  public InMemoryDatabase build() {
    return new InMemoryDatabase(this.name, this.version);
  }

}
