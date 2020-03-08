package grevend.persistence.lite.extensions.sql;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SqlDaoFactory extends DaoFactory {

    public SqlDaoFactory(@NotNull Database database) {
        super(database);
    }

    @Override
    public @NotNull <A, B> Dao<A, B> createDao(@NotNull Class<A> entity, @NotNull Class<B> keyClass,
                                               List<Triplet<Class<?>, String, String>> keys) {
        return new Dao<>() {

            @Override
            public boolean createAll(@NotNull Collection<A> entities) {
                return false;
            }

            @Override
            public Optional<A> retrieve(@NotNull B key) {
                return Optional.empty();
            }

            @Override
            public Optional<A> retrieve(@NotNull Map<String, ?> keyValuePairs) {
                return Optional.empty();
            }

            @Override
            public @NotNull Set<A> retrieveAll() {
                return Set.of();
            }

            @Override
            public boolean updateAll(@NotNull Collection<A> entities) {
                return false;
            }

            @Override
            public boolean deleteAll(@NotNull Collection<A> entities) {
                return false;
            }

        };
    }

}
