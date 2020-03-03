package grevend.persistence.lite.dao;

import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.sql.PrimaryKey;
import grevend.persistence.lite.util.Pair;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static grevend.persistence.lite.entity.EntityManager.viableFields;

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

    public @NotNull <A> List<Triplet<Class<?>, String, String>> getPrimaryKeys(@NotNull Class<A> entity) {
        return Arrays.stream(entity.getDeclaredFields()).filter(viableFields)
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public @NotNull <A, B> Dao<A, B> ofEntity(@NotNull Class<A> entity, @NotNull Class<B> keyClass)
            throws IllegalArgumentException {
        var keys = getPrimaryKeys(entity);
        if (keys.size() <= 0) {
            throw new IllegalArgumentException(
                    "Every entity must possess a primary key annotated with " +
                            PrimaryKey.class.getCanonicalName() + ".");
        } else {
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

    public @NotNull <A> Dao<A, Object> ofEntity(@NotNull Class<A> entity) {
        return ofEntity(entity, Object.class);
    }

}

