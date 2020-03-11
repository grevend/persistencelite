package grevend.persistence.lite.entity;

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

import static grevend.persistence.lite.util.Utils.isConstructorViable;
import static grevend.persistence.lite.util.Utils.isFieldViable;

public class EntityClass<E> {

    private final Class<E> entityClass;
    private Optional<Constructor<E>> entityConstructor = Optional.empty();
    private List<Field> entityFields = null;
    private List<Triplet<Class<?>, String, String>> entityAttributes;

    private EntityClass(@NotNull Class<E> entityClass) {
        this.entityClass = entityClass;
        this.entityAttributes = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> EntityClass<E> of(@NotNull Class<E> entityClass) throws IllegalArgumentException {
        if (!entityClass.isAnnotationPresent(Entity.class))
            throw new IllegalArgumentException("Class " + entityClass.getCanonicalName()
                    + " must be annotated with @" + Entity.class.getCanonicalName());
        return (EntityClass<E>) EntityClassCache.getInstance()
                .entityClasses.computeIfAbsent(entityClass, clazz -> new EntityClass<>(entityClass));
    }

    public boolean isSerializable() {
        return Serializable.class.isAssignableFrom(this.entityClass);
    }

    public boolean hasViableConstructor() {
        return this.getConstructor().isPresent();
    }

    public boolean hasViableFields() {
        return this.getFields().size() > 0;
    }

    private @NotNull List<Field> getFields() {
        if (this.entityFields == null) {
            this.entityFields =
                    Arrays.stream(this.entityClass.getDeclaredFields()).filter(isFieldViable)
                            .collect(Collectors.toList());
        }
        return this.entityFields;
    }

    @SuppressWarnings("unchecked")
    private @NotNull Optional<Constructor<E>> getConstructor() {
        if (this.entityConstructor.isEmpty()) {
            List<Constructor<?>> constructors = Arrays.stream(this.entityClass.getDeclaredConstructors())
                    .filter(isConstructorViable).collect(Collectors.toList());
            this.entityConstructor =
                    constructors.size() > 0 ? Optional.ofNullable((Constructor<E>) constructors.get(0)) :
                            Optional.empty();
        }
        return this.entityConstructor;
    }

    private @NotNull List<Triplet<Class<?>, String, String>> getAttributes() {
        return Arrays.stream(this.entityClass.getDeclaredFields())
                .filter(isFieldViable)
                .map(field -> new Triplet<Class<?>, String, String>(field.getType(), field.getName(),
                        field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name() :
                                field.getName())).collect(Collectors.toList());
    }

    public @NotNull List<String> getAttributeNames() {
        return Arrays.stream(this.entityClass.getDeclaredFields())
                .filter(isFieldViable)
                .map(field -> field.isAnnotationPresent(Attribute.class) ?
                        field.getAnnotation(Attribute.class).name() : field.getName()).collect(Collectors.toList());
    }

    public @NotNull List<String> getOriginalAttributeNames() {
        return Arrays.stream(this.entityClass.getDeclaredFields())
                .filter(isFieldViable)
                .map(field -> field.getAnnotation(Attribute.class).name()).collect(Collectors.toList());
    }

    public @NotNull List<Object> getAttributeValues(@NotNull E entity) {
        return Arrays.stream(this.entityClass.getDeclaredFields()).filter(isFieldViable).map(field -> {
            try {
                boolean isAccessible = field.canAccess(entity);
                field.setAccessible(true);
                var obj = this.processResultObject(field.get(entity));
                field.setAccessible(isAccessible);
                return obj;
            } catch (IllegalAccessException e) {
                System.out.println("... ... ...");
                return null;
            }
        }).collect(Collectors.toList());
    }

    private Object processResultObject(Object obj) {
        if (obj instanceof String) {
            obj = "'" + obj.toString() + "'";
        } else if (obj instanceof Option) {
            obj = ((Option<?>) obj).isPresent() ? this.processResultObject(((Option<?>) obj).get()) : "null";
        } else if (obj instanceof Optional) {
            obj = ((Optional<?>) obj).isPresent() ? this.processResultObject(((Optional<?>) obj).get()) : "null";
        }
        return obj;
    }

    private @NotNull E construct() throws IllegalStateException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (this.hasViableConstructor()) {
            Constructor<?> unwrappedConstructor = this.entityConstructor.get();
            if (this.entityAttributes.isEmpty()) {
                this.entityAttributes.addAll(this.getAttributes());
            }
            boolean isAccessible = unwrappedConstructor.isAccessible();
            unwrappedConstructor.setAccessible(true);
            E obj = this.entityClass.cast(unwrappedConstructor.newInstance());
            unwrappedConstructor.setAccessible(isAccessible);
            return obj;
        } else {
            throw new IllegalArgumentException("Class " + this.entityClass.getCanonicalName()
                    + " must declare an empty constructor.");
        }
    }

    private @NotNull E construct(@NotNull ThrowingFunction<String, ?> values)
            throws IllegalStateException, IllegalArgumentException {
        try {
            E obj = this.construct();
            for (Triplet<Class<?>, String, String> attribute : this.entityAttributes) {
                Field field = this.entityClass.getField(attribute.getB());
                boolean isAccessible = field.canAccess(obj);
                field.setAccessible(true);
                if (attribute.getA().equals(Option.class)) {
                    if (values.apply(attribute.getC()) instanceof Serializable ||
                            values.apply(attribute.getC()) == null) {
                        field.set(obj, Option.of((Serializable) values.apply(attribute.getC())));
                    } else {
                        throw new IllegalStateException(
                                "Value of " + attribute.getC() + " with type " + attribute.getA().getCanonicalName() +
                                        " does not implement " + Serializable.class.getCanonicalName() + ".");
                    }
                } else if (attribute.getA().isAssignableFrom(Optional.class)) {
                    if (!(obj instanceof Serializable)) {
                        field.set(obj,
                                attribute.getA().equals(Optional.class) ?
                                        Optional.ofNullable(values.apply(attribute.getC())) :
                                        values.apply(attribute.getC()));
                    } else {
                        throw new IllegalStateException(
                                "Entity " + this.entityClass.getCanonicalName() + " implements " +
                                        Serializable.class.getCanonicalName() + " but includes attribute " +
                                        attribute.getB() +
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
            throw new IllegalStateException("Construction of " + this.entityClass.getCanonicalName() + " failed.",
                    exception);
        }
    }

    public @NotNull E construct(@NotNull Map<String, Object> values)
            throws IllegalStateException {
        return this.construct(values::get);
    }

    public @NotNull E construct(@NotNull ResultSet resultSet)
            throws IllegalStateException {
        return this.construct(resultSet::getObject);
    }

    public @NotNull Class<E> getEntityClass() {
        return this.entityClass;
    }

    public @NotNull String getEntityName() {
        return this.entityClass.getAnnotation(Entity.class).name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        EntityClass<?> that = (EntityClass<?>) o;
        return this.getEntityClass().equals(that.getEntityClass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getEntityClass());
    }

    private static final class EntityClassCache {

        private static EntityClassCache entityClassCache;

        private Map<Class<?>, EntityClass<?>> entityClasses;

        private EntityClassCache() {
            this.entityClasses = new HashMap<>();
        }

        private static EntityClassCache getInstance() {
            if (entityClassCache == null) {
                entityClassCache = new EntityClassCache();
            }
            return entityClassCache;
        }

    }

}
