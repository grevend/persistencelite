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

package grevend.persistencelite.internal.entity.factory;

import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.entity.EntityType;
import grevend.persistencelite.internal.util.Utils;
import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.function.ThrowingFunction;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import org.postgresql.util.PGInterval;

/**
 * @author David Greven
 * @see EntityMetadata
 * @see EntityProperty
 * @see EntityType
 * @since 0.2.0
 */
public final class EntityFactory {

    private static final Map<Class<?>, TypeMarshaller<Object, Object>> marshallers = Map.of(
        Date.class, date -> date == null ? null : ((Date) date).toLocalDate(),
        Time.class, time -> time == null ? null : ((Time) time).toLocalTime(),
        Timestamp.class, timestamp ->
            timestamp == null ? null : ((Timestamp) timestamp).toLocalDateTime(),
        PGInterval.class, interval ->
            interval == null ? null : Duration.ofSeconds(((PGInterval) interval).getWholeSeconds())
    );

    private static final Map<Class<?>, TypeMarshaller<Object, Object>> unmarshallers = Map.of(
        LocalDate.class, date -> date == null ? null : Date.valueOf((LocalDate) date),
        LocalTime.class, time -> time == null ? null : Time.valueOf((LocalTime) time),
        LocalDateTime.class, dateTime ->
            dateTime == null ? null : Timestamp.valueOf((LocalDateTime) dateTime),
        Duration.class, duration -> {
            if (duration == null) { return null; }
            var d = (Duration) duration;
            return new PGInterval(0, 0, (int) d.toDays(), (int) d.toHours(),
                (int) d.toMinutes(), d.getSeconds() + d.getNano() / 1e9);
        }
    );

    /**
     * @param entityMetadata
     * @param properties
     * @param props
     * @param <E>
     *
     * @return
     *
     * @throws Throwable
     * @see EntityMetadata
     * @see Map
     * @since 0.2.3
     */
    @NotNull
    public static <E> E construct(@NotNull EntityMetadata<E> entityMetadata, @NotNull final Map<String, Object> properties, boolean props) throws Throwable {
        return switch (entityMetadata.entityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> constructRecord(entityMetadata, properties.keySet(), props,
                key -> Utils.extract(key, properties, List.of()));
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
        if (entityMetadata.constructor() == null) {
            throw new IllegalArgumentException();
        }
        final var propertyNames = entityMetadata.declaredProperties().stream()
            .map(prop -> props ? prop.propertyName() : prop.fieldName())
            .collect(Collectors.toUnmodifiableList());
        if (!properties.containsAll(propertyNames)) {
            final var missingProperties = new ArrayList<>(propertyNames);
            missingProperties.removeAll(properties);
            throw new IllegalArgumentException(
                "Missing properties: " + missingProperties.toString());
        }
        final var propsMeta = entityMetadata.declaredProperties().stream().filter(
            prop -> props ? propertyNames.contains(prop.propertyName())
                : propertyNames.contains(prop.fieldName()))
            .collect(Collectors.toUnmodifiableList());

        final List<Object> propertyValues = new ArrayList<>();
        for (var prop : propsMeta) {
            var name = props ? prop.propertyName() : prop.fieldName();
            propertyValues.add(marshall(values.apply(name), prop.type(), marshallers));
        }
        return (E) entityMetadata.constructor().invokeWithArguments(propertyValues);
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
    @Contract("null, _, _ -> null")
    private static Object marshall(@Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, TypeMarshaller<Object, Object>> marshallerMap) {
        if (type.isEnum() && value instanceof String) {
            try {
                var method = type.getMethod("valueOf", String.class);
                method.setAccessible(true);
                return method.invoke(null, ((String) value).toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
                return value;
            }
        }
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
        return switch (entityMetadata.entityType()) {
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
        return entityMetadata.superTypes().stream()
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
        superTypeMetadata.declaredProperties().forEach(property -> {
            try {
                properties.put(property.propertyName(),
                    unmarshall(Objects.requireNonNull(property.getter()).invoke(entity)));
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
        entityMetadata.superTypes().forEach(metadata -> metadata.declaredProperties()
            .forEach(prop -> superPropNames.add(prop.fieldName())));
        var props = entityMetadata.declaredProperties().stream()
            .filter(prop -> !superPropNames.contains(prop.fieldName()) || prop.identifier() != null
                || prop.copy()).collect(Collectors.toCollection(ArrayList::new));
        props.forEach(property -> {
            try {
                properties.put(property.propertyName(),
                    unmarshall(Objects.requireNonNull(property.getter()).invoke(entity)));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return properties;
    }

    /**
     * @param value
     *
     * @return
     *
     * @since 0.2.0
     */
    @Nullable
    @Contract("null -> null")
    private static Object unmarshall(@Nullable Object value) {
        if (value != null && Objects.requireNonNull(value).getClass().isEnum()) {
            return value.toString().toLowerCase();
        }
        return value;
    }

}
