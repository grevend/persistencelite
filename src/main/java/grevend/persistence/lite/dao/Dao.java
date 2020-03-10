package grevend.persistence.lite.dao;

import grevend.persistence.lite.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface Dao<E> {

    boolean create(@NotNull E entity);

    default boolean createAll(@NotNull Collection<E> entities) {
        return entities.stream().allMatch(this::create);
    }

    Optional<E> retrieveByKey(@NotNull Tuple key);

    default Optional<E> retrieve(@NotNull Object... keys) {
        return retrieveByKey(Tuple.of(keys));
    }

    Collection<E> retrieveByAttributes(@NotNull Map<String, ?> attributes);

    @NotNull Collection<E> retrieveAll();

    default boolean update(@NotNull E entity) {
        return delete(entity) && create(entity);
    }

    default boolean updateAll(@NotNull Collection<E> entities) {
        return entities.stream().allMatch(this::update);
    }

    boolean delete(@NotNull E entity);

    default boolean deleteAll(@NotNull Collection<E> entities) {
        return entities.stream().allMatch(this::delete);
    }

}
