package grevend.persistence.lite;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.database.DatabaseBuilder;
import grevend.persistence.lite.database.DatabaseBuilderException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

public final class Persistence {

  public static @NotNull <B extends DatabaseBuilder<D>, D extends Database> B databaseBuilder(
      @NotNull Class<B> databaseBuilder, @NotNull String name, int version) {
    try {
      Constructor<B> constructor = databaseBuilder
          .getConstructor(String.class, Integer.TYPE);
      return constructor.newInstance(name, version);
    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      throw new DatabaseBuilderException("DatabaseBuilder construction failed.", e);
    }
  }

}
