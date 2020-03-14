package grevend.persistence.lite.util;

import org.jetbrains.annotations.NotNull;

public abstract class PersistenceException extends RuntimeException {

  public PersistenceException(@NotNull String message) {
    super(message);
  }

  public PersistenceException(@NotNull String message, Object... args) {
    super(String.format(message, args));
  }

  public PersistenceException(@NotNull String message, @NotNull Throwable cause) {
    super(message, cause);
  }

  public PersistenceException(@NotNull String message, @NotNull Throwable cause, Object... args) {
    super(String.format(message, args), cause);
  }

}
