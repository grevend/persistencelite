package grevend.persistence.lite.database.inmemory;

import grevend.persistence.lite.util.PersistenceException;
import org.jetbrains.annotations.NotNull;

public class InMemoryDatabaseException extends PersistenceException {

  public InMemoryDatabaseException(@NotNull String message, Object... args) {
    super(message, args);
  }

}
