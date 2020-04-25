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
import grevend.persistencelite.entity.lookup.ComponentLookup;
import grevend.persistencelite.entity.lookup.InterfaceLookup;
import grevend.persistencelite.entity.lookup.RecordLookup;
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

/**
 * @param <E>
 *
 * @author David Greven
 * @see EntityProperty
 * @see EntityType
 * @see MethodHandle
 * @since 0.2.0
 */
public final class EntityMetadata<E> {

    private final ComponentLookup<E, ?> lookup;

    private final Class<E> entityClass;
    private final List<EntityMetadata<?>> superTypes;
    private final Collection<EntityProperty> properties;
    private final Collection<EntityProperty> identifiers;
    private final EntityType entityType;
    private MethodHandle constructor;

    /**
     * @param entityClass
     * @param entityType
     *
     * @see Class
     * @see EntityType
     * @since 0.2.0
     */
    private EntityMetadata(@NotNull Class<E> entityClass, @NotNull EntityType entityType) {
        this.entityClass = entityClass;
        this.superTypes = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.identifiers = new ArrayList<>();
        this.constructor = null;
        this.entityType = entityType;

        this.lookup = switch (this.entityType) {
            case CLASS -> null;
            case INTERFACE -> new InterfaceLookup<>();
            case RECORD -> new RecordLookup<>();
        };
    }

    /**
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @see Class
     * @since 0.2.0
     */
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

    @Contract("_ -> param1")
    public static <E> @NotNull EntityMetadata<E> inferRelationTypes(@NotNull EntityMetadata<E> metadata) {
        metadata.getDeclaredRelations().stream().map(EntityProperty::relation).filter(
            relation -> Objects.requireNonNull(relation).getType() == EntityRelationType.UNKNOWN)
            .forEach(relation -> {
                EntityMetadata.of(relation.getTargetEntity()).getDeclaredRelations().stream()
                    .map(EntityProperty::relation).filter(Objects::nonNull)
                    .filter(
                        relation2 -> relation2.getTargetEntity().equals(metadata.getEntityClass()))
                    .forEach(relation2 -> {
                        relation.setCircularDependency(true);
                        relation2.setCircularDependency(true);
                    });
            });

        metadata.getDeclaredRelations().forEach(prop -> {
            EntityMetadata.of(Objects.requireNonNull(prop.relation()).getTargetEntity())
                .getDeclaredRelations().forEach(prop2 -> {
                if (prop.type().isAssignableFrom(Collection.class)) {
                    if (prop2.type().isAssignableFrom(Collection.class)) {
                        Objects.requireNonNull(prop.relation())
                            .setType(EntityRelationType.MANY_TO_MANY);
                        Objects.requireNonNull(prop2.relation())
                            .setType(EntityRelationType.MANY_TO_MANY);
                    } else {
                        Objects.requireNonNull(prop.relation())
                            .setType(EntityRelationType.ONE_TO_MANY);
                        Objects.requireNonNull(prop2.relation())
                            .setType(EntityRelationType.ONE_TO_ONE);
                    }
                } else {
                    if (prop2.type().isAssignableFrom(Collection.class)) {
                        Objects.requireNonNull(prop.relation())
                            .setType(EntityRelationType.ONE_TO_ONE);
                        Objects.requireNonNull(prop2.relation())
                            .setType(EntityRelationType.ONE_TO_MANY);
                    } else {
                        Objects.requireNonNull(prop.relation())
                            .setType(EntityRelationType.ONE_TO_ONE);
                        Objects.requireNonNull(prop2.relation())
                            .setType(EntityRelationType.ONE_TO_ONE);
                    }
                }
            });
        });

        return metadata;
    }

    /**
     * @return
     *
     * @see Entity
     * @since 0.2.0
     */
    @NotNull
    public String getName() {
        return this.getEntityClass().getAnnotation(Entity.class).name();
    }

    /**
     * @return
     *
     * @see Class
     * @since 0.2.0
     */
    @NotNull
    @Contract(pure = true)
    public Class<E> getEntityClass() {
        return this.entityClass;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityMetadata
     * @see ComponentLookup#lookupSuperTypes(EntityMetadata)
     * @since 0.2.0
     */
    @NotNull
    @Contract(pure = true)
    public Collection<EntityMetadata<?>> getDeclaredSuperTypes() {
        if (this.superTypes.isEmpty()) {
            this.superTypes.addAll(this.lookup.lookupSuperTypes(this));
        }
        return this.superTypes;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityMetadata
     * @see #getDeclaredSuperTypes()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityMetadata<?>> getSuperTypes() {
        Collection<EntityMetadata<?>> list = this.getDeclaredSuperTypes().stream()
            .flatMap(superType -> superType.getSuperTypes().stream())
            .collect(Collectors.toList());
        list.addAll(this.getDeclaredSuperTypes());
        return list;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see ComponentLookup#lookupProperties(EntityMetadata)
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> getDeclaredProperties() {
        if (this.properties.isEmpty()) {
            this.properties.addAll(this.lookup.lookupProperties(this));
        }
        return this.properties;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see #getDeclaredProperties()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> getUniqueProperties() {
        var allProps = this.getSuperTypes().stream()
            .flatMap(superType -> superType.getDeclaredProperties().stream())
            .map(EntityProperty::propertyName).collect(Collectors.toUnmodifiableSet());
        return this.getDeclaredProperties().stream().filter(
            prop -> !allProps.contains(prop.propertyName()) || prop.identifier() != null ||
                prop.copy()).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see #getDeclaredProperties()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> getDeclaredIdentifiers() {
        if (this.identifiers.isEmpty()) {
            this.identifiers.addAll(
                this.getDeclaredProperties().stream().filter(prop -> prop.identifier() != null)
                    .collect(Collectors.toList()));
        }
        return this.identifiers;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see #getDeclaredProperties()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> getDeclaredRelations() {
        return this.getDeclaredProperties().stream().filter(prop -> prop.relation() != null)
            .collect(Collectors.toList());
    }

    /**
     * @return
     *
     * @see MethodHandle
     * @see ComponentLookup#lookupConstructor(EntityMetadata)
     * @since 0.2.0
     */
    @Nullable
    public MethodHandle getConstructor() {
        if (this.constructor == null) {
            this.constructor = this.lookup.lookupConstructor(this);
        }
        return this.constructor;
    }

    /**
     * @return
     *
     * @see EntityType
     * @since 0.2.0
     */
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
    @SuppressWarnings("unused")
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
    public boolean isValid() {
        return !this.getDeclaredIdentifiers().isEmpty() && this.getDeclaredIdentifiers()
            .containsAll(
                this.getDeclaredSuperTypes().stream()
                    .flatMap(superType -> superType.getDeclaredIdentifiers().stream())
                    .collect(Collectors.toUnmodifiableSet())) && this.getDeclaredSuperTypes()
            .stream()
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
            ", superTypes=" + this.getDeclaredSuperTypes() +
            ", properties=" + this.getDeclaredProperties() +
            ", constructor=" + this.getConstructor() +
            '}';
    }

    /**
     * @return
     *
     * @see #getEntityType()
     * @see #getEntityClass()
     * @see #getDeclaredSuperTypes()
     * @see #getDeclaredProperties()
     * @see #getConstructor()
     * @since 0.2.0
     */
    @NotNull
    public String toStructuredString() {
        return """
            EntityMetadata {
                 entityType=%s
                 entityClass=%s
                 superTypes=%s
                 properties=%s
                 constructor=%s
            }"""
            .formatted(this.getEntityType(), this.getEntityClass(), this.getDeclaredSuperTypes(),
                this.getDeclaredProperties(), this.getConstructor());
    }

    /**
     * @author David Greven
     * @see EntityMetadata
     * @since 0.2.0
     */
    static final class EntityMetadataCache {

        private static final Object MUTEX = new Object();
        private static volatile EntityMetadataCache INSTANCE;
        private final Map<Class<?>, EntityMetadata<?>> entityMetadataMap;

        /**
         * @since 0.2.0
         */
        @Contract(pure = true)
        private EntityMetadataCache() {
            this.entityMetadataMap = new HashMap<>();
        }

        /**
         * @return
         *
         * @since 0.2.0
         */
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

        /**
         * @return
         *
         * @see Map
         * @see EntityMetadata
         * @since 0.2.0
         */
        @NotNull
        @Contract(pure = true)
        private Map<Class<?>, EntityMetadata<?>> getEntityMetadataMap() {
            return this.entityMetadataMap;
        }

        /**
         * @since 0.2.0
         */
        @SuppressWarnings("unused")
        void clearCache() {
            this.entityMetadataMap.clear();
        }

    }

}
