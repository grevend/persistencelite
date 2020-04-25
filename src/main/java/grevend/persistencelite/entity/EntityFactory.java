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

import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.function.ThrowingFunction;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see EntityMetadata
 * @see EntityProperty
 * @see EntityType
 * @since 0.2.0
 */
public final class EntityFactory {

    /**
     * @param entityMetadata
     * @param properties
     * @param <E>
     *
     * @return
     *
     * @throws Throwable
     * @see EntityMetadata
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    public static <E> E construct(@NotNull EntityMetadata<E> entityMetadata, @NotNull final Map<String, Object> properties) throws Throwable {
        return switch (entityMetadata.getEntityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> constructRecord(entityMetadata, properties.keySet(), false,
                properties::get);
        };
    }

    /**
     * @param entityMetadata
     * @param values
     * @param <E>
     *
     * @return
     *
     * @throws Throwable
     * @see EntityMetadata
     * @see ResultSet
     * @since 0.2.0
     */
    @NotNull
    public static <E> E construct(@NotNull EntityMetadata<E> entityMetadata, @NotNull final ResultSet values) throws Throwable {
        return switch (entityMetadata.getEntityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> constructRecord(entityMetadata,
                entityMetadata.getDeclaredProperties().stream().map(EntityProperty::propertyName)
                    .collect(Collectors.toList()), true, values::getObject);
        };
    }

    /**
     * @param entityMetadata
     * @param properties
     * @param props
     * @param values
     * @param <E>
     *
     * @return
     *
     * @throws Throwable
     * @see EntityMetadata
     * @see Collection
     * @see ThrowingFunction
     * @since 0.2.0
     */
    @NotNull
    @SuppressWarnings("unchecked")
    private static <E> E constructRecord(@NotNull EntityMetadata<E> entityMetadata, @NotNull Collection<String> properties, boolean props, @NotNull ThrowingFunction<String, Object> values) throws Throwable {
        if (entityMetadata.getConstructor() == null) {
            throw new IllegalArgumentException();
        }
        final var propertyNames = entityMetadata.getDeclaredProperties().stream()
            .map(prop -> props ? prop.propertyName() : prop.fieldName())
            .collect(Collectors.toUnmodifiableList());
        if (!properties.containsAll(propertyNames)) {
            final var missingProperties = new ArrayList<>(propertyNames);
            missingProperties.removeAll(properties);
            throw new IllegalArgumentException(
                "Missing properties: " + missingProperties.toString());
        }
        final List<Object> propertyValues = new ArrayList<>();
        for (var name : propertyNames) {
            propertyValues.add(marshall(values.apply(name), Map.of()));
        }
        return (E) entityMetadata.getConstructor().invokeWithArguments(propertyValues);
    }

    /**
     * @param value
     * @param marshallerMap
     *
     * @return
     *
     * @since 0.2.0
     */
    @Nullable
    @Contract("null, _ -> null")
    private static Object marshall(@Nullable Object value, @NotNull Map<Class<?>, TypeMarshaller<Object, Object>> marshallerMap) {
        //TODO add enum support
        if (value == null) {
            return null;
        } else if (marshallerMap.containsKey(value.getClass())) {
            return marshallerMap.get(value.getClass()).marshall(value);
        }
        return value;
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    public static <E> Collection<Map<String, Object>> deconstruct(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        return switch (entityMetadata.getEntityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> deconstructRecord(entityMetadata, entity);
        };
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Collection<Map<String, Object>> deconstructRecord(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        Collection<Map<String, Object>> components = new ArrayList<>(
            deconstructRecordSuperTypes(entityMetadata, entity));
        components.add(deconstructRecordComponents(entityMetadata, entity));
        return components;
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Collection<Map<String, Object>> deconstructRecordSuperTypes(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        return entityMetadata.getSuperTypes().stream()
            .map(superType -> deconstructRecordSuperType(superType, entity))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @param superTypeMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Map<String, Object> deconstructRecordSuperType(@NotNull EntityMetadata<?> superTypeMetadata, @NotNull E entity) {
        Map<String, Object> properties = new HashMap<>();
        superTypeMetadata.getDeclaredProperties().forEach(property -> {
            try {
                properties.put(property.propertyName(),
                    Objects.requireNonNull(property.getter()).invoke(entity));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return properties;
    }

    /**
     * @param entityMetadata
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private static <E> Map<String, Object> deconstructRecordComponents(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        Map<String, Object> properties = new HashMap<>();
        var superPropNames = new HashSet<String>();
        entityMetadata.getSuperTypes().forEach(metadata -> metadata.getDeclaredProperties()
            .forEach(prop -> superPropNames.add(prop.fieldName())));
        var props = entityMetadata.getDeclaredProperties().stream()
            .filter(prop -> !superPropNames.contains(prop.fieldName()) || prop.identifier() != null
                || prop.copy()).collect(Collectors.toCollection(ArrayList::new));
        props.forEach(property -> {
            try {
                properties.put(property.propertyName(),
                    Objects.requireNonNull(property.getter()).invoke(entity));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return properties;
    }

}
