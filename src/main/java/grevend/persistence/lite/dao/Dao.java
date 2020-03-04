package grevend.persistence.lite.dao;

import grevend.persistence.lite.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Dao<E, K> {

    default boolean create(@NotNull E entity) {
        return createAll(List.of(entity));
    }

    boolean createAll(@NotNull Collection<E> entities);

    Optional<E> retrieve(@NotNull K key);

    Optional<E> retrieve(@NotNull Collection<Pair<String, ?>> keyValuePairs);

    @NotNull List<E> retrieveAll();

    default boolean update(@NotNull E entity) {
        return updateAll(List.of(entity));
    }

    boolean updateAll(@NotNull Collection<E> entities);

    default boolean delete(@NotNull E entity) {
        return deleteAll(List.of(entity));
    }

    boolean deleteAll(@NotNull Collection<E> entities);

}
