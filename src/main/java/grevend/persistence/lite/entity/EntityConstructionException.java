package grevend.persistence.lite.entity;

import grevend.persistence.lite.util.PersistenceException;
import org.jetbrains.annotations.NotNull;

public class EntityConstructionException extends PersistenceException {

  public EntityConstructionException(@NotNull String message,
      @NotNull Throwable cause, Object... args) {
    super(message, cause, args);
  }

}
