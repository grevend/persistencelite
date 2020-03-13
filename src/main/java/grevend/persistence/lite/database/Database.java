package grevend.persistence.lite.database;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.entity.EntityImplementationException;
import grevend.persistence.lite.util.PrimaryKey;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Utils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class Database implements AutoCloseable {

  private final String name, user, password;
  private final int version;

  public Database(@NotNull String name, int version, @NotNull String user,
      @NotNull String password) {
    this.name = name;
    this.version = version;
    this.user = user;
    this.password = password;
    this.onStart();
  }

  public String getName() {
    return this.name;
  }

  public int getVersion() {
    return this.version;
  }

  public String getUser() {
    return this.user;
  }

  public String getPassword() {
    return this.password;
  }

  public abstract @NotNull URI getURI() throws URISyntaxException;

  public abstract @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entity,
      @NotNull List<Triplet<Class<?>, String, String>> keys);

  public void onStart() {
  }

  public void onStop() {
  }

  private @NotNull <A> List<Triplet<Class<?>, String, String>> getPrimaryKeys(
      @NotNull EntityClass<A> entity) {
    return Arrays.stream(entity.getEntityClass().getDeclaredFields())
        .filter(Utils.isFieldViable)
        .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
        .map(field -> Triplet.<Class<?>, String, String>of(field.getType(), field.getName(),
            (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class)
                .name() :
                field.getName()))).collect(Collectors.toList());
  }

  private @NotNull <A> Dao<A> getDao(@NotNull EntityClass<A> entity) {
    var keys = this.getPrimaryKeys(entity);
    if (keys.size() <= 0) {
      throw new EntityImplementationException(
          "Every entity must possess a primary key annotated with %s.",
          PrimaryKey.class.getCanonicalName());
    } else {
      return this.createDao(entity, keys);
    }
  }

  public @NotNull <A> Dao<A> getDao(@NotNull Class<A> clazz) {
    return this.getDao(EntityClass.of(clazz));
  }

  @Override
  public void close() {
    this.onStop();
  }

}
