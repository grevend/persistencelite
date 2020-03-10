package grevend.persistence.lite.entity;

import grevend.persistence.lite.util.Ignore;
import grevend.persistence.lite.util.Option;
import grevend.persistence.lite.util.ThrowingFunction;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntityClass<E> {

    private final Class<E> entityClass;
    private List<Triplet<Class<?>, String, String>> entityAttributes;

    private EntityClass(@NotNull Class<E> entityClass) {
        this.entityClass = entityClass;
        this.entityAttributes = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> EntityClass<E> of(@NotNull Class<E> entityClass) throws IllegalArgumentException {
        if (entityClass.isAnnotationPresent(Entity.class)) {
            return (EntityClass<E>) EntityClassCache.getInstance().entityClasses
                    .computeIfAbsent(entityClass, clazz -> new EntityClass<>(entityClass));
        } else {
            throw new IllegalArgumentException("Class " + entityClass.getCanonicalName()
                    + " must be annotated with @" + Entity.class.getCanonicalName());
        }
    }

    private @NotNull Predicate<Constructor<?>> isConstructorViable() {
        return constructor -> constructor.getParameterCount() == 0 && !constructor.isSynthetic();
    }

    private @NotNull Predicate<Field> isFieldViable() {
        return field -> !field.isSynthetic()
                && !field.isAnnotationPresent(Ignore.class)
                && !Modifier.isAbstract(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers());
    }

    private @NotNull List<Triplet<Class<?>, String, String>> getFields() {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(this.isFieldViable())
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        (field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName()))).collect(Collectors.toList());
    }

    private @NotNull Optional<Constructor<?>> getConstructor() {
        List<Constructor<?>> constructors = Arrays.stream(entityClass.getDeclaredConstructors())
                .filter(this.isConstructorViable())
                .collect(Collectors.toList());
        if (constructors.size() != 0) {
            return Optional.ofNullable(constructors.get(0));
        } else {
            return Optional.empty();
        }
    }

    private @NotNull E construct() throws IllegalStateException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Optional<Constructor<?>> constructor = this.getConstructor();
        if (constructor.isPresent()) {
            Constructor<?> unwrappedConstructor = constructor.get();
            if (this.entityAttributes.isEmpty()) {
                this.entityAttributes.addAll(this.getFields());
            }
            boolean isAccessible = unwrappedConstructor.isAccessible();
            unwrappedConstructor.setAccessible(true);
            E obj = entityClass.cast(unwrappedConstructor.newInstance());
            unwrappedConstructor.setAccessible(isAccessible);
            return obj;
        } else {
            throw new IllegalArgumentException("Class " + entityClass.getCanonicalName()
                    + " must declare an empty constructor.");
        }
    }

    private @NotNull E construct(@NotNull ThrowingFunction<String, ?> values)
            throws IllegalStateException, IllegalArgumentException {
        try {
            E obj = construct();
            for (Triplet<Class<?>, String, String> attribute : this.entityAttributes) {
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
                } else if (attribute.getA().isAssignableFrom(Optional.class)) {
                    if (!(obj instanceof Serializable)) {
                        field.set(obj,
                                attribute.getA().equals(Optional.class) ?
                                        Optional.ofNullable(values.apply(attribute.getC())) :
                                        values.apply(attribute.getC()));
                    } else {
                        throw new IllegalStateException("Entity " + entityClass.getCanonicalName() + " implements " +
                                Serializable.class.getCanonicalName() + " but includes attribute " + attribute.getB() +
                                " of unserializable type " + Optional.class.getCanonicalName() + ".");
                    }
                } else {
                    if (attribute.getA().isAssignableFrom(Serializable.class)) {
                        if (values.apply(attribute.getC()) instanceof Serializable) {
                            field.set(obj, Option.of((Serializable) values.apply(attribute.getC())));
                        } else {
                            throw new IllegalStateException("Value of " + attribute.getC() + " does not implement " +
                                    Serializable.class.getCanonicalName() + ".");
                        }
                    } else {
                        field.set(obj, values.apply(attribute.getC()));
                    }
                }
                field.setAccessible(isAccessible);
            }
            return obj;
        } catch (Exception exception) {
            throw new IllegalStateException("Construction of " + entityClass.getCanonicalName() + " failed.",
                    exception);
        }
    }

    public @NotNull E construct(@NotNull Map<String, Object> values)
            throws IllegalStateException {
        return construct(values::get);
    }

    public @NotNull E construct(@NotNull ResultSet resultSet)
            throws IllegalStateException {
        return construct(resultSet::getObject);
    }

    public @NotNull Class<E> getEntityClass() {
        return entityClass;
    }

    public @NotNull String getEntityName() {
        return entityClass.getAnnotation(Entity.class).name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityClass<?> that = (EntityClass<?>) o;
        return getEntityClass().equals(that.getEntityClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntityClass());
    }

    private static final class EntityClassCache {

        private static EntityClassCache entityClassCache;

        private Map<Class<?>, EntityClass<?>> entityClasses;

        private EntityClassCache() {
            this.entityClasses = new HashMap<>();
        }

        public static EntityClassCache getInstance() {
            if (entityClassCache == null) {
                entityClassCache = new EntityClassCache();
            }
            return entityClassCache;
        }

    }

}
