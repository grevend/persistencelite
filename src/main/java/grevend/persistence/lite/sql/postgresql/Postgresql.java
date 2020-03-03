package grevend.persistence.lite.sql.postgresql;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Postgresql implements Dao<Object, Object> {

    @Override
    public boolean createAll(@NotNull Collection<Object> entities) {
        return false;
    }

    @Override
    public Optional<Object> retrieve(@NotNull Object key) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> retrieve(@NotNull Collection<Pair<String, ?>> keyValuePairs) {
        return Optional.empty();
    }

    @Override
    public @NotNull List<Object> retrieveAll() {
        return List.of();
    }

    @Override
    public boolean updateAll(@NotNull Collection<Object> entities) {
        return false;
    }

    @Override
    public boolean deleteAll(@NotNull Collection<Object> entities) {
        return false;
    }

}
