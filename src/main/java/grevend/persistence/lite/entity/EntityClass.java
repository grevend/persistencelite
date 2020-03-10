package grevend.persistence.lite.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityClass<E> {

    private final Class<E> entityClass;

    private EntityClass(@NotNull Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    public static @NotNull <E> EntityClass<E> of(@NotNull Class<E> entityClass) throws IllegalArgumentException {
        if (entityClass.isAnnotationPresent(Entity.class)) {
            return new EntityClass<>(entityClass);
        } else {
            throw new IllegalArgumentException("Class " + entityClass.getCanonicalName()
                    + " must be annotated with @" + Entity.class.getCanonicalName());
        }
    }

    public @NotNull Class<E> getEntityClass() {
        return entityClass;
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

}
