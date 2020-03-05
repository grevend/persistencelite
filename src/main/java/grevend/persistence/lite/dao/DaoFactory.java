package grevend.persistence.lite.dao;

import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.Entity;
import grevend.persistence.lite.util.PrimaryKey;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static grevend.persistence.lite.entity.EntityManager.viableFields;

public abstract class DaoFactory {

    protected @NotNull <A> List<Triplet<Class<?>, String, String>> getPrimaryKeys(@NotNull Class<A> entity) {
        return Arrays.stream(entity.getDeclaredFields()).filter(viableFields)
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    public abstract @NotNull <A, B> Dao<A, B> createDao(@NotNull Class<A> entity, @NotNull Class<B> keyClass,
                                                        List<Triplet<Class<?>, String, String>> keys);

    @SuppressWarnings("unchecked")
    public @NotNull <A, B> Dao<A, B> ofEntity(@NotNull Class<A> entity, @NotNull Class<B> keyClass)
            throws IllegalArgumentException {
        if (entity.isAnnotationPresent(Entity.class)) {
            var keys = getPrimaryKeys(entity);
            if (keys.size() <= 0) {
                throw new IllegalArgumentException(
                        "Every entity must possess a primary key annotated with " +
                                PrimaryKey.class.getCanonicalName() + ".");
            } else {
                return createDao(entity, keyClass, keys);
            }
        } else {
            throw new IllegalArgumentException("Class " + entity.getCanonicalName()
                    + " must be annotated with @" + Entity.class.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    public @NotNull <A, B> Dao<A, B> ofEntity(@NotNull Class<A> entity) {
        var keys = getPrimaryKeys(entity);
        return ofEntity(entity, keys.size() == 1 ? (Class<B>) keys.get(0).getClass() :
                (keys.size() > 1 ? (Class<B>) List.class : (Class<B>) Object.class));
    }

}

