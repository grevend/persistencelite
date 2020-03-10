package grevend.persistence.lite.extensions.sql;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SqlDaoFactory extends DaoFactory {

    public SqlDaoFactory(@NotNull Database database) {
        super(database);
    }

    @Override
    public @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entity,
                                         @NotNull List<Triplet<Class<?>, String, String>> keys) {
        return new Dao<>() {

            @Override
            public boolean create(@NotNull A entity) {
                return false;
            }

            @Override
            public Optional<A> retrieveByKey(@NotNull Tuple key) {
                return Optional.empty();
            }

            @Override
            public Collection<A> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
                return List.of();
            }

            @Override
            public @NotNull Collection<A> retrieveAll() {
                return List.of();
            }

            @Override
            public boolean delete(@NotNull A entity) {
                return false;
            }

            @Override
            public boolean deleteByKey(@NotNull Tuple key) {
                return false;
            }

        };
    }

}
