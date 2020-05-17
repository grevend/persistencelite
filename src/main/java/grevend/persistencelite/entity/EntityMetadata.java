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

import static grevend.common.Memoizer.memoize;

import grevend.common.jacoco.Generated;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.entity.EntityRelationType;
import grevend.persistencelite.internal.entity.EntityType;
import grevend.persistencelite.internal.entity.lookup.EntityLookup;
import grevend.persistencelite.internal.entity.lookup.InterfaceLookup;
import grevend.persistencelite.internal.entity.lookup.RecordLookup;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

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

    private final EntityLookup<E, ?> lookup;

    private final Class<E> entityClass;
    private final List<EntityMetadata<?>> superTypes;
    private final Function<EntityMetadata<?>, Collection<EntityMetadata<?>>> subTypeLookup;
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
        this.subTypeLookup = memoize(self -> {
            Reflections reflections = new Reflections("grevend.main");
            return reflections.getSubTypesOf(self.entityClass()).stream().map(EntityMetadata::of)
                .collect(Collectors.toUnmodifiableSet());
        });
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
                return (EntityMetadata<E>) EntityMetadataCache.instance().cache()
                    .computeIfAbsent(entity, clazz -> new EntityMetadata<>(entity,
                        entity.isRecord() ? EntityType.RECORD : EntityType.INTERFACE));
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public static Collection<EntityMetadata<?>> entities(@NotNull String packageScope) {
        Reflections reflections = new Reflections(packageScope);
        return reflections.getTypesAnnotatedWith(Entity.class).stream().map(EntityMetadata::of)
            .collect(Collectors.toUnmodifiableSet());
    }

    public static <E> void inferRelationTypes(@NotNull EntityMetadata<E> metadata) {
        metadata.declaredRelations().stream().map(EntityProperty::relation).filter(
            relation -> Objects.requireNonNull(relation).getType() == EntityRelationType.UNKNOWN)
            .forEach(
                relation -> EntityMetadata.of(relation.getTargetEntity()).declaredRelations()
                    .stream()
                    .map(EntityProperty::relation).filter(Objects::nonNull)
                    .filter(
                        relation2 -> relation2.getTargetEntity().equals(metadata.entityClass()))
                    .forEach(relation2 -> {
                        relation.setCircularDependency(true);
                        relation2.setCircularDependency(true);
                    }));

        metadata.declaredRelations().forEach(prop ->
            EntityMetadata.of(Objects.requireNonNull(prop.relation()).getTargetEntity())
                .declaredRelations().forEach(prop2 -> {
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
            }));

    }

    /**
     * @return
     *
     * @see Entity
     * @since 0.2.0
     */
    @NotNull
    public String name() {
        return this.entityClass().getAnnotation(Entity.class).name();
    }

    /**
     * @return
     *
     * @see Class
     * @since 0.2.0
     */
    @NotNull
    @Contract(pure = true)
    public Class<E> entityClass() {
        return this.entityClass;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityMetadata
     * @see EntityLookup#lookupSuperTypes(EntityMetadata)
     * @since 0.2.0
     */
    @NotNull
    @Contract(pure = true)
    public Collection<EntityMetadata<?>> declaredSuperTypes() {
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
     * @see #declaredSuperTypes()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityMetadata<?>> superTypes() {
        Collection<EntityMetadata<?>> list = this.declaredSuperTypes().stream()
            .flatMap(superType -> superType.superTypes().stream())
            .collect(Collectors.toList());
        list.addAll(this.declaredSuperTypes());
        return list;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityMetadata<?>> subTypes() {
        return this.subTypeLookup.apply(this);
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityMetadata<?>> concreteSubTypes() {
        return this.subTypes().stream().filter(subType ->
            subType.entityType() == EntityType.RECORD ||
                subType.entityType() == EntityType.CLASS)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see EntityLookup#lookupProperties(EntityMetadata)
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> declaredProperties() {
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
     * @see #declaredProperties()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> uniqueProperties() {
        var allSuperProps = this.superTypes().stream()
            .flatMap(superType -> superType.declaredProperties().stream())
            .map(EntityProperty::fieldName).collect(Collectors.toUnmodifiableSet());
        return this.declaredProperties().stream().filter(
            prop -> !allSuperProps.contains(prop.fieldName()) || prop.identifier() != null ||
                prop.copy()).filter(prop -> prop.relation() == null)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> properties() {
        Collection<EntityProperty> list = this.declaredSuperTypes().stream()
            .flatMap(superType -> superType.superTypes().stream())
            .flatMap(superType -> superType.declaredProperties().stream()).distinct()
            .collect(Collectors.toList());
        list.addAll(this.declaredProperties());
        return list;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see #declaredProperties()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> declaredIdentifiers() {
        if (this.identifiers.isEmpty()) {
            this.identifiers.addAll(
                this.declaredProperties().stream().filter(prop -> prop.identifier() != null)
                    .collect(Collectors.toList()));
        }
        return this.identifiers;
    }

    /**
     * @return
     *
     * @see Collection
     * @see EntityProperty
     * @see #declaredProperties()
     * @since 0.2.0
     */
    @NotNull
    public Collection<EntityProperty> declaredRelations() {
        return this.declaredProperties().stream().filter(prop -> prop.relation() != null)
            .collect(Collectors.toList());
    }

    /**
     * @return
     *
     * @see MethodHandle
     * @see EntityLookup#lookupConstructor(EntityMetadata)
     * @since 0.2.0
     */
    @Nullable
    public MethodHandle constructor() {
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
    public EntityType entityType() {
        return this.entityType;
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
    public boolean valid() {
        return !this.declaredIdentifiers().isEmpty() && this.declaredIdentifiers()
            .containsAll(
                this.declaredSuperTypes().stream()
                    .flatMap(superType -> superType.declaredIdentifiers().stream())
                    .collect(Collectors.toUnmodifiableSet())) && this.declaredSuperTypes()
            .stream()
            .allMatch(EntityMetadata::valid) && (
            (this.entityType() == EntityType.RECORD && this.constructor() != null) || (
                this.entityType() == EntityType.INTERFACE && this.constructor() == null));
    }

    @Override
    @Generated
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || this.getClass() != o.getClass()) { return false; }
        EntityMetadata<?> that = (EntityMetadata<?>) o;
        return this.entityClass().equals(that.entityClass()) &&
            this.entityType() == that.entityType();
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.entityClass(), this.entityType());
    }

    @NotNull
    @Override
    @Generated
    @Contract(pure = true)
    public String toString() {
        return "EntityMetadata{" +
            "entityType=" + this.entityType() +
            ", entityClass=" + this.entityClass() +
            ", superTypes=" + this.declaredSuperTypes() +
            ", properties=" + this.declaredProperties() +
            ", constructor=" + this.constructor() +
            '}';
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
        private static EntityMetadataCache instance() {
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
        private Map<Class<?>, EntityMetadata<?>> cache() {
            return this.entityMetadataMap;
        }

        /**
         * @since 0.2.0
         */
        @SuppressWarnings("unused")
        void clear() {
            this.entityMetadataMap.clear();
        }

    }

}
