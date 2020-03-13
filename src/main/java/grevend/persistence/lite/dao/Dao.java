package grevend.persistence.lite.dao;

import grevend.persistence.lite.util.Tuple;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface Dao<E> {

  boolean create(@NotNull E entity);

  default boolean createAll(@NotNull Collection<E> entities) {
    return entities.stream().allMatch(this::create);
  }

  Optional<E> retrieveByKey(@NotNull Tuple key);

  Collection<E> retrieveByAttributes(@NotNull Map<String, ?> attributes);

  @NotNull Collection<E> retrieveAll();

  default @NotNull Stream<E> stream() {
    return this.retrieveAll().stream();
  }

  default @NotNull Stream<E> parallelStream() {
    return this.retrieveAll().parallelStream();
  }

  default boolean update(@NotNull E entity) {
    return this.delete(entity) && this.create(entity);
  }

  default boolean updateAll(@NotNull Collection<E> entities) {
    return entities.stream().allMatch(this::update);
  }

  boolean delete(@NotNull E entity);

  boolean deleteByKey(@NotNull Tuple key);

  default boolean deleteAll(@NotNull Collection<E> entities) {
    return entities.stream().allMatch(this::delete);
  }

}
