package grevend.persistence.lite.dao;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public final class DaoFactory {

    private static DaoFactory instance;

    private DaoFactory() {
    }

    public static synchronized @NotNull DaoFactory getInstance() {
        if (instance == null) {
            instance = new DaoFactory();
        }
        return instance;
    }

    public @NotNull <A> Dao<A, Integer> ofEntity(@NotNull Class<A> entity) throws IllegalArgumentException {
        return new Dao<>() {

            @Override
            public boolean create(Collection<A> entities) {
                return false;
            }

            @Override
            public A retrieve(Integer id) {
                return null;
            }

            @Override
            public List<A> retrieveAll() {
                return null;
            }

            @Override
            public boolean update(A e) {
                return false;
            }

            @Override
            public boolean delete(Integer id) {
                return false;
            }

        };
    }

}

