package grevend.persistence.lite.dao;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.PrimaryKey;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DaoFactory {

    protected final Database database;

    public DaoFactory(@NotNull Database database) {
        this.database = database;
    }

    protected @NotNull <A> List<Triplet<Class<?>, String, String>> getPrimaryKeys(@NotNull EntityClass<A> entity) {
        return Arrays.stream(entity.getEntityClass().getDeclaredFields())
                .filter(this.database.getExtension().isFieldViable())
                .filter(field -> field.isAnnotationPresent(PrimaryKey.class))
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    public abstract @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entity,
                                                  @NotNull List<Triplet<Class<?>, String, String>> keys);

    public @NotNull <A> Dao<A> ofEntity(@NotNull EntityClass<A> entity)
            throws IllegalArgumentException {
        var keys = getPrimaryKeys(entity);
        if (keys.size() <= 0) {
            throw new IllegalArgumentException(
                    "Every entity must possess a primary key annotated with " +
                            PrimaryKey.class.getCanonicalName() + ".");
        } else {
            return createDao(entity, keys);
        }
    }

    public @NotNull <A> Dao<A> ofEntity(@NotNull Class<A> clazz) throws IllegalArgumentException {
        return this.ofEntity(EntityClass.of(clazz));
    }

}

