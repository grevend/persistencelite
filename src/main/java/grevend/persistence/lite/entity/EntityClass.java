package grevend.persistence.lite.entity;

import static grevend.persistence.lite.util.Utils.isConstructorViable;
import static grevend.persistence.lite.util.Utils.isFieldViable;

import grevend.jacoco.Generated;
import grevend.persistence.lite.util.Option;
import grevend.persistence.lite.util.PrimaryKey;
import grevend.persistence.lite.util.ThrowingFunction;
import grevend.persistence.lite.util.Triplet;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

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
  public static @NotNull <E> EntityClass<E> of(@NotNull Class<E> entityClass) {
    if (!entityClass.isAnnotationPresent(Entity.class)) {
      throw new EntityImplementationException(
          "Class %s must be annotated with @%s" + Entity.class.getCanonicalName());
    }
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

  private @NotNull String getAttributeName(@NotNull Field field) {
    return field.isAnnotationPresent(Attribute.class) ? field.getAnnotation(Attribute.class).name()
        : field.getName();
  }

  private @NotNull List<Triplet<Class<?>, String, String>> getAttributes() {
    return this.getFields().stream().map(
        field -> Triplet.<Class<?>, String, String>of(field.getType(), field.getName(),
            this.getAttributeName(field))).collect(Collectors.toList());
  }

  public @NotNull List<String> getAttributeNames() {
    return this.getFields().stream().map(this::getAttributeName).collect(Collectors.toList());
  }

  public @NotNull List<String> getOriginalAttributeNames() {
    return this.getFields().stream().map(field -> field.getAnnotation(Attribute.class).name())
        .collect(Collectors.toList());
  }

  public @NotNull List<Object> getAttributeValues(@NotNull E entity) {
    return this.getFields().stream().map(field -> {
      try {
        boolean isAccessible = field.canAccess(entity);
        field.setAccessible(true);
        var obj = this.processResultObject(field.get(entity));
        field.setAccessible(isAccessible);
        return obj;
      } catch (IllegalAccessException e) {
        throw new EntityConstructionException("Construction of %s failed.", e,
            this.entityClass.getCanonicalName());
      }
    }).collect(Collectors.toList());
  }

  public @NotNull List<Triplet<Class<?>, String, String>> getPrimaryKeys() {
    return this.getFields().stream().filter(field -> field.isAnnotationPresent(PrimaryKey.class))
        .map(field -> Triplet.<Class<?>, String, String>of(field.getType(), field.getName(),
            this.getAttributeName(field))).collect(Collectors.toList());
  }

  private Object processResultObject(Object obj) {
    if (obj instanceof Option) {
      obj = ((Option<?>) obj).isPresent() ? this.processResultObject(((Option<?>) obj).get())
          : "null";
    } else if (obj instanceof Optional) {
      obj = ((Optional<?>) obj).isPresent() ? this.processResultObject(((Optional<?>) obj).get())
          : "null";
    }
    return obj;
  }

  private @NotNull E construct() {
    if (!this.hasViableConstructor()) {
      throw new EntityImplementationException("Class %s must declare an empty constructor.",
          this.entityClass.getCanonicalName());
    } else {
      try {
        Constructor<?> unwrappedConstructor = this.entityConstructor.get();
        if (this.entityAttributes.isEmpty()) {
          this.entityAttributes.addAll(this.getAttributes());
        }
        boolean isAccessible = unwrappedConstructor.isAccessible();
        unwrappedConstructor.setAccessible(true);
        E obj = this.entityClass.cast(unwrappedConstructor.newInstance());
        unwrappedConstructor.setAccessible(isAccessible);
        return obj;
      } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
        throw new EntityConstructionException("Construction of %s failed.", e,
            this.entityClass.getCanonicalName());
      }
    }
  }

  private @NotNull E construct(@NotNull ThrowingFunction<String, ?> values) {
    E obj = this.construct();
    try {
      for (Triplet<Class<?>, String, String> attribute : this.entityAttributes) {
        Field field = this.entityClass.getField(attribute.getB());
        boolean isAccessible = field.canAccess(obj);
        field.setAccessible(true);
        if (attribute.getA().equals(Option.class)) {
          if (!(values.apply(attribute.getC()) instanceof Serializable)
              && values.apply(attribute.getC()) != null) {
            throw new EntityImplementationException(
                "Value of %s with type %s does not implement %s.", attribute.getC(),
                attribute.getA().getCanonicalName(), Serializable.class.getCanonicalName());
          } else {
            field.set(obj, Option.of((Serializable) values.apply(attribute.getC())));
          }
        } else if (attribute.getA().isAssignableFrom(Optional.class)) {
          if (obj instanceof Serializable) {
            throw new EntityImplementationException(
                "Entity %s implements %s but includes attribute %s of unserializable type %s",
                this.entityClass.getCanonicalName(), Serializable.class.getCanonicalName(),
                attribute.getB(), Optional.class.getCanonicalName());
          } else {
            field.set(obj, attribute.getA().equals(Optional.class) ? Optional
                .ofNullable(values.apply(attribute.getC())) : values.apply(attribute.getC()));
          }
        } else {
          if (attribute.getA().isAssignableFrom(Serializable.class)) {
            if (!(values.apply(attribute.getC()) instanceof Serializable)) {
              throw new EntityImplementationException("Value of %s does not implement %s.",
                  attribute.getC(), Serializable.class.getCanonicalName());
            } else {
              field.set(obj, Option.of((Serializable) values.apply(attribute.getC())));
            }
          } else {
            field.set(obj, values.apply(attribute.getC()));
          }
        }
        field.setAccessible(isAccessible);
      }
      return obj;
    } catch (Exception e) {
      throw new EntityConstructionException("Construction of %s failed.", e,
          this.entityClass.getCanonicalName());
    }
  }

  public @NotNull E construct(@NotNull Map<String, Object> values) {
    return this.construct(values::get);
  }

  public @NotNull E construct(@NotNull ResultSet resultSet) {
    return this.construct(resultSet::getObject);
  }

  public @NotNull Class<E> getEntityClass() {
    return this.entityClass;
  }

  public @NotNull String getEntityName() {
    return this.entityClass.getAnnotation(Entity.class).name();
  }

  @Override
  @Generated
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    EntityClass<?> that = (EntityClass<?>) o;
    return this.getEntityClass().equals(that.getEntityClass());
  }

  @Override
  @Generated
  public int hashCode() {
    return Objects.hash(this.getEntityClass());
  }

  @Override
  public String toString() {
    return "EntityClass{" + "entityClass=" + this.entityClass + ", entityAttributes="
        + this.entityAttributes + '}';
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
