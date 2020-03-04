package grevend.persistence.lite.postgresql;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.util.Pair;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PostgresqlDaoFactory extends DaoFactory {

    private static PostgresqlDaoFactory instance;

    private PostgresqlDaoFactory() {
    }

    public static synchronized @NotNull PostgresqlDaoFactory getInstance() {
        if (instance == null) {
            instance = new PostgresqlDaoFactory();
        }
        return instance;
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
            public Optional<A> retrieve(@NotNull Collection<Pair<String, ?>> keyValuePairs) {
                return Optional.empty();
            }

            @Override
            public @NotNull List<A> retrieveAll() {
                return List.of();
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
