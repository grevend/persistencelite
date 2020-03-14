package grevend.persistence.lite.database.inmemory;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Pair;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import grevend.persistence.lite.util.Utils;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

public class InMemoryDatabase extends Database {

  private Map<EntityClass<?>, List<Object>> storage;

  public InMemoryDatabase(@NotNull String name, int version) {
    super(name, version);
    this.storage = new HashMap<>();
  }

  @Override
  public @NotNull URI getURI() {
    return new File(this.getName() + ".ser").toURI();
  }

  public Map<EntityClass<?>, List<Object>> getStorage() {
    return this.storage;
  }

  private <A> boolean checkKey(@NotNull A entity, @NotNull Map<String, ?> keyValuePairs,
      @NotNull Collection<Triplet<Class<?>, String, String>> keys) {
    return keys.stream().allMatch(k -> {
      try {
        Field field = entity.getClass().getField(k.getB());
        field.setAccessible(true);
        return Objects.equals(field.get(entity), keyValuePairs.get(k.getB()));
      } catch (NoSuchFieldException | IllegalAccessException e) {
        return false;
      }
    });
  }

  @Override
  public @NotNull <E> Dao<E> createDao(@NotNull EntityClass<E> entityClass,
      @NotNull List<Triplet<Class<?>, String, String>> keys) {
    if (!Serializable.class.isAssignableFrom(entityClass.getEntityClass())) {
      throw new InMemoryDatabaseException(
          "InMemoryDaoFactory only supports entities that implement the %s interface.",
          Serializable.class.getCanonicalName());
    }
    if (!keys.stream().allMatch(
        key -> Serializable.class.isAssignableFrom(key.getA()) || Utils.primitives
            .contains(key.getA()))) {
      throw new InMemoryDatabaseException(
          "InMemoryDaoFactory only supports entities with keys that implement the %s interface.",
          Serializable.class.getCanonicalName());
    }
    if (!this.storage.containsKey(entityClass)) {
      this.storage.put(entityClass, new ArrayList<>());
    }
    return new Dao<>() {

      @Override
      public boolean create(@NotNull E entity) {
        if (!InMemoryDatabase.this.storage.get(entityClass).contains(entity)) {
          InMemoryDatabase.this.storage.get(entityClass).add(entity);
          return true;
        }
        return false;
      }

      @Override
      @SuppressWarnings("unchecked")
      public Optional<E> retrieveByKey(@NotNull Tuple key) {
        Map<?, ?> attributes = IntStream.range(0, key.count()).mapToObj(i -> Pair
            .of((String & Serializable) keys.get(i).getB(),
                (Serializable) key.get(i, keys.get(i).getA()))).collect(Pair.toMap());
        Collection<E> res = this.retrieveByAttributes((Map<String, ?>) attributes);
        return res == null || res.size() != 1 ? Optional.empty()
            : Optional.ofNullable(res.iterator().next());
      }

      @Override
      public Collection<E> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
        return this.retrieveAll().stream()
            .filter(entity -> InMemoryDatabase.this.checkKey(entity, attributes, keys))
            .collect(Collectors.toList());
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull Collection<E> retrieveAll() {
        return (Collection<E>) InMemoryDatabase.this.storage.get(entityClass);
      }

      @Override
      public boolean delete(@NotNull E entity) {
        if (InMemoryDatabase.this.storage.containsKey(entityClass)) {
          InMemoryDatabase.this.storage.get(entityClass).remove(entity);
          return true;
        }
        return false;
      }

      @Override
      public boolean deleteByKey(@NotNull Tuple key) {
        var entity = this.retrieveByKey(key);
        return entity.isPresent() && this.delete(entity.get());
      }

      @Override
      public boolean deleteByAttributes(@NotNull Map<String, ?> attributes) {
        return this.deleteAll(this.retrieveByAttributes(attributes));
      }

    };
  }

}
