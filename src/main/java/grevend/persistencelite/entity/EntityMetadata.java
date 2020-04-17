/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.entity;

import grevend.jacoco.Generated;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EntityMetadata<E> {

    private final Class<E> entityClass;
    private final List<EntityMetadata<?>> superTypes;
    private final Collection<EntityProperty> properties;
    private final Collection<EntityProperty> identifiers;
    private final EntityType entityType;
    private MethodHandle constructor;

    private EntityMetadata(@NotNull Class<E> entityClass, @NotNull EntityType entityType) {
        this.entityClass = entityClass;
        this.superTypes = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.identifiers = new ArrayList<>();
        this.constructor = null;
        this.entityType = entityType;
    }

    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <E> EntityMetadata<E> of(@NotNull Class<E> entity) {
        if (!entity.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException(
                "Entity %s must be annotated with @%s" + Entity.class.getCanonicalName());
        } else {
            if (entity.isRecord() || entity.isInterface()) {
                return (EntityMetadata<E>) EntityMetadataCache.getInstance().getEntityMetadataMap()
                    .computeIfAbsent(entity, clazz -> new EntityMetadata<>(entity,
                        entity.isRecord() ? EntityType.RECORD : EntityType.INTERFACE));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @NotNull
    public String getName() {
        return this.getEntityClass().getAnnotation(Entity.class).name();
    }

    @NotNull
    @Contract(pure = true)
    public Class<E> getEntityClass() {
        return this.entityClass;
    }

    @NotNull
    @Contract(pure = true)
    public Collection<EntityMetadata<?>> getSuperTypes() {
        if (this.superTypes.isEmpty()) {
            this.superTypes.addAll(EntityLookup.lookupSuperTypes(this));
        }
        return this.superTypes;
    }

    @NotNull
    public Collection<EntityProperty> getProperties() {
        if (this.properties.isEmpty()) {
            this.properties.addAll(EntityLookup.lookupProperties(this));
        }
        return this.properties;
    }

    @NotNull
    public Collection<EntityProperty> getIdentifiers() {
        if (this.identifiers.isEmpty()) {
            this.identifiers.addAll(this.getProperties().stream().filter(EntityProperty::id)
                .collect(Collectors.toList()));
        }
        return this.identifiers;
    }

    @Nullable
    public MethodHandle getConstructor() {
        if (this.constructor == null) {
            this.constructor = EntityLookup.lookupConstructor(this);
        }
        return this.constructor;
    }

    @NotNull
    @Contract(pure = true)
    public EntityType getEntityType() {
        return this.entityType;
    }

    /**
     * Checks if the entity is serializable.
     *
     * @return True if the entity class is serializable.
     *
     * @see Serializable
     * @since 0.2.0
     */
    public boolean isSerializable() {
        return Serializable.class.isAssignableFrom(this.getEntityClass());
    }

    /**
     * Checks whether:
     * <ul>
     *     <li>at least one identifier has been set</li>
     *     <li>all identifiers of the super types are also present</li>
     *     <li>all super types are valid</li>
     *     <li>the record has a constructor</li>
     *     <li>the interfaces have no constructor</li>
     * </ul>
     *
     * @return True if the entity and all its super types are valid.
     *
     * @see EntityType
     * @since 0.2.0
     */
    boolean isValid() {
        return !this.getIdentifiers().isEmpty() && this.getIdentifiers().containsAll(
            this.getSuperTypes().stream().flatMap(superType -> superType.getIdentifiers().stream())
                .collect(Collectors.toUnmodifiableSet())) && this.getSuperTypes().stream()
            .allMatch(EntityMetadata::isValid) && (
            (this.getEntityType() == EntityType.RECORD && this.getConstructor() != null) || (
                this.getEntityType() == EntityType.INTERFACE && this.getConstructor() == null));
    }

    @Override
    @Generated
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || this.getClass() != o.getClass()) { return false; }
        EntityMetadata<?> that = (EntityMetadata<?>) o;
        return this.getEntityClass().equals(that.getEntityClass()) &&
            this.getEntityType() == that.getEntityType();
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.getEntityClass(), this.getEntityType());
    }

    @NotNull
    @Override
    @Generated
    @Contract(pure = true)
    public String toString() {
        return "EntityMetadata{" +
            "entityType=" + this.getEntityType() +
            ", entityClass=" + this.getEntityClass() +
            ", superTypes=" + this.getSuperTypes() +
            ", properties=" + this.getProperties() +
            ", constructor=" + this.getConstructor() +
            '}';
    }

    @NotNull
    String toStructuredString() {
        return """
            EntityMetadata {
                 entityType=%s
                 entityClass=%s
                 superTypes=%s
                 properties=%s
                 constructor=%s
            }""".formatted(this.getEntityType(), this.getEntityClass(), this.getSuperTypes(),
            this.getProperties(), this.getConstructor());
    }

    static final class EntityMetadataCache {

        private static final Object MUTEX = new Object();
        private static volatile EntityMetadataCache INSTANCE;
        private final Map<Class<?>, EntityMetadata<?>> entityMetadataMap;

        @Contract(pure = true)
        private EntityMetadataCache() {
            this.entityMetadataMap = new HashMap<>();
        }

        @NotNull
        private static EntityMetadataCache getInstance() {
            var result = INSTANCE;
            if (result == null) {
                synchronized (MUTEX) {
                    result = INSTANCE;
                    if (result == null) {
                        INSTANCE = result = new EntityMetadataCache();
                    }
                }
            }
            return result;
        }

        @NotNull
        @Contract(pure = true)
        private Map<Class<?>, EntityMetadata<?>> getEntityMetadataMap() {
            return this.entityMetadataMap;
        }

        @SuppressWarnings("unused")
        void clearCache() {
            this.entityMetadataMap.clear();
        }

    }

}
