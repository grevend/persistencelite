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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class EntityFactory {

    @NotNull
    public static <E> E construct(@NotNull EntityMetadata<E> entityMetadata, @NotNull Map<String, Object> properties) throws Throwable {
        return switch (entityMetadata.getEntityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> constructRecord(entityMetadata, properties);
        };
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static <E> E constructRecord(@NotNull EntityMetadata<E> entityMetadata, @NotNull Map<String, Object> properties) throws Throwable {
        if (entityMetadata.getConstructor() == null) {
            throw new IllegalArgumentException();
        }
        final var propertyNames = entityMetadata.getProperties().stream()
            .map(EntityProperty::fieldName).collect(Collectors.toUnmodifiableList());
        if (!properties.keySet().containsAll(propertyNames)) {
            final var missingProperties = new ArrayList<>(propertyNames);
            missingProperties.removeAll(properties.keySet());
            throw new IllegalArgumentException(
                "Missing properties: " + missingProperties.toString());
        }
        final var propertyValues = new ArrayList<>();
        propertyNames.forEach(name -> propertyValues.add(properties.get(name)));
        return (E) entityMetadata.getConstructor().invokeWithArguments(propertyValues);
    }

    @NotNull
    public static <E> Collection<Map<String, Object>> deconstruct(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        return switch (entityMetadata.getEntityType()) {
            case CLASS, INTERFACE -> throw new UnsupportedOperationException();
            case RECORD -> deconstructRecord(entityMetadata, entity);
        };
    }

    @NotNull
    private static <E> Collection<Map<String, Object>> deconstructRecord(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        Collection<Map<String, Object>> components = new ArrayList<>(
            deconstructRecordSuperTypes(entityMetadata, entity));
        components.add(deconstructRecordComponents(entityMetadata, entity));
        return components;
    }

    @NotNull
    private static <E> Collection<Map<String, Object>> deconstructRecordSuperTypes(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        Collection<Map<String, Object>> components = new ArrayList<>();
        entityMetadata.getSuperTypes()
            .forEach(superType -> components.add(deconstructRecordSuperType(superType, entity)));
        return components;
    }

    @NotNull
    private static <E> Map<String, Object> deconstructRecordSuperType(@NotNull EntityMetadata<?> superTypeMetadata, @NotNull E entity) {
        Map<String, Object> properties = new HashMap<>();
        superTypeMetadata.getProperties().forEach(property -> {
            try {
                properties.put(property.propertyName(),
                    Objects.requireNonNull(property.getter()).invoke(entity));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return properties;
    }

    @NotNull
    private static <E> Map<String, Object> deconstructRecordComponents(@NotNull EntityMetadata<E> entityMetadata, @NotNull E entity) {
        Map<String, Object> properties = new HashMap<>();
        var superPropNames = new HashSet<String>();
        entityMetadata.getSuperTypes().forEach(metadata -> metadata.getProperties()
            .forEach(prop -> superPropNames.add(prop.fieldName())));
        var props = entityMetadata.getProperties().stream()
            .filter(prop -> !superPropNames.contains(prop.fieldName()) || prop.id() || prop.copy())
            .collect(Collectors.toCollection(ArrayList::new));
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
