package grevend.persistence.lite.entity;

import grevend.persistence.lite.util.PersistenceException;
import org.jetbrains.annotations.NotNull;

public class EntityImplementationException extends PersistenceException {

  public EntityImplementationException(@NotNull String message) {
    super(message);
  }

  public EntityImplementationException(@NotNull String message, Object... args) {
    super(message, args);
  }

}
