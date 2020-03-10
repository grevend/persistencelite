package grevend.persistence.lite.entity;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.util.Option;
import grevend.persistence.lite.util.ThrowingFunction;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public final class EntityManager {

    private final Database database;
    private Map<Class<?>, List<Triplet<Class<?>, String, String>>> entityAttributes;

    public EntityManager(@NotNull Database database) {
        this.database = database;
        this.entityAttributes = new HashMap<>();
    }

    private @NotNull List<Triplet<Class<?>, String, String>> getFields(@NotNull EntityClass<?> entity) {
        return Arrays.stream(entity.getEntityClass().getDeclaredFields())
                .filter(this.database.getExtension().isFieldViable())
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    private @NotNull Optional<Constructor<?>> getConstructor(@NotNull EntityClass<?> entity) {
        List<Constructor<?>> constructors = Arrays.stream(entity.getEntityClass().getDeclaredConstructors())
                .filter(this.database.getExtension().isConstructorViable())
                .collect(Collectors.toList());
        if (constructors.size() != 0) {
            return Optional.ofNullable(constructors.get(0));
        } else {
            return Optional.empty();
        }
    }

    private @NotNull <A> A constructEntity(@NotNull EntityClass<A> entity) throws IllegalStateException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Optional<Constructor<?>> constructor = this.getConstructor(entity);
        if (constructor.isPresent()) {
            Constructor<?> unwrappedConstructor = constructor.get();
            this.entityAttributes.putIfAbsent(entity.getEntityClass(), this.getFields(entity));
            boolean isAccessible = unwrappedConstructor.isAccessible();
            unwrappedConstructor.setAccessible(true);
            A obj = entity.getEntityClass().cast(unwrappedConstructor.newInstance());
            unwrappedConstructor.setAccessible(isAccessible);
            return obj;
        } else {
            throw new IllegalArgumentException("Class " + entity.getEntityClass().getCanonicalName()
                    + " must declare an empty public or protected constructor");
        }
    }

    public @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull Map<String, Object> values)
            throws IllegalStateException {
        return constructEntity(EntityClass.of(entity), values::get);
    }

    public @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull ResultSet resultSet)
            throws IllegalStateException {
        return constructEntity(EntityClass.of(entity), resultSet::getObject);
    }

    private @NotNull <A> A constructEntity(@NotNull EntityClass<A> entity, @NotNull ThrowingFunction<String, ?> values)
            throws IllegalStateException, IllegalArgumentException {
        Class<A> entityClass = entity.getEntityClass();
        try {
            A obj = constructEntity(entity);
            if (this.entityAttributes.containsKey(entityClass)) {
                for (Triplet<Class<?>, String, String> attribute : this.entityAttributes.get(entityClass)) {
                    Field field = entityClass.getField(attribute.getB());
                    boolean isAccessible = field.canAccess(obj);
                    field.setAccessible(true);
                    if (attribute.getA().equals(Option.class)) {
                        if (values.apply(attribute.getC()) instanceof Serializable) {
                            field.set(obj, Option.of((Serializable) values.apply(attribute.getC())));
                        } else {
                            throw new IllegalStateException("Value of " + attribute.getC() + " does not implement " +
                                    Serializable.class.getCanonicalName() + ".");
                        }
                    }
                    if (attribute.getA().equals(Optional.class) && !Serializable.class.isAssignableFrom(entityClass)) {
                        field.set(obj,
                                attribute.getA().equals(Optional.class) ?
                                        Optional.ofNullable(values.apply(attribute.getC())) :
                                        values.apply(attribute.getC()));
                    } else {
                        throw new IllegalStateException("Entity " + entityClass.getCanonicalName() + " implements " +
                                Serializable.class.getCanonicalName() + " but includes attribute " + attribute.getB() +
                                " of unserializable type " + Optional.class.getCanonicalName() + ".");
                    }
                    field.setAccessible(isAccessible);
                }
            } else {
                throw new IllegalStateException(
                        "EntityManager does not recognize " + entityClass.getCanonicalName() + ".");
            }
            return obj;
        } catch (Exception exception) {
            throw new IllegalStateException("Construction of " + entityClass.getCanonicalName() + " failed.",
                    exception);
        }
    }

}
