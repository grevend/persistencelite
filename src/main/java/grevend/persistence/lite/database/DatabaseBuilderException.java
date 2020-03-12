package grevend.persistence.lite.database;

import grevend.persistence.lite.util.PersistenceException;
import org.jetbrains.annotations.NotNull;

public class DatabaseBuilderException extends PersistenceException {

  public DatabaseBuilderException(@NotNull String message) {
    super(message);
  }

  public DatabaseBuilderException(@NotNull String message, @NotNull Throwable cause) {
    super(message, cause);
  }

}
