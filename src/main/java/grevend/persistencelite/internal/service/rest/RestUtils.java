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

package grevend.persistencelite.internal.service.rest;

import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.service.rest.RestService;
import grevend.persistencelite.util.TypeMarshaller;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @since 0.6.4
 */
public final class RestUtils {

    public static void initTypeMarshalling(@NotNull RestService service) {
        service.registerTypeMarshaller(Byte.TYPE, Byte::valueOf);
        service.registerTypeMarshaller(Byte.class, Byte::valueOf);
        service.registerTypeMarshaller(Short.TYPE, Short::valueOf);
        service.registerTypeMarshaller(Short.class, Short::valueOf);
        service.registerTypeMarshaller(Integer.TYPE, Integer::valueOf);
        service.registerTypeMarshaller(Integer.class, Integer::valueOf);
        service.registerTypeMarshaller(Long.TYPE, Long::valueOf);
        service.registerTypeMarshaller(Long.class, Long::valueOf);
        service.registerTypeMarshaller(Float.TYPE, Float::valueOf);
        service.registerTypeMarshaller(Float.class, Float::valueOf);
        service.registerTypeMarshaller(Double.TYPE, Double::valueOf);
        service.registerTypeMarshaller(Double.class, Double::valueOf);
        service.registerTypeMarshaller(Character.TYPE, val -> val != null ?
            (val.length() == 1 ? val.charAt(0) : null) : null);
        service.registerTypeMarshaller(Character.class, val -> val != null ?
            (val.length() == 1 ? val.charAt(0) : null) : null);
        service.registerTypeMarshaller(Boolean.TYPE, Boolean::valueOf);
        service.registerTypeMarshaller(Boolean.class, Boolean::valueOf);
        service.registerTypeMarshaller(BigInteger.class, BigInteger::new);
        service.registerTypeMarshaller(BigDecimal.class, BigDecimal::new);
        service.registerTypeMarshaller(Duration.class, Duration::parse);
        service.registerTypeMarshaller(LocalTime.class, LocalTime::parse);
        service.registerTypeMarshaller(LocalDate.class, LocalDate::parse);
        service.registerTypeMarshaller(LocalDateTime.class, LocalDateTime::parse);
        service.registerTypeMarshaller(ZonedDateTime.class, ZonedDateTime::parse);
        service.registerTypeMarshaller(String.class, s -> Objects.equals(s, "null") ? null : s);
    }

    /**
     * @param entityMetadata
     * @param value
     * @param type
     * @param unmarshallerMap
     *
     * @return
     *
     * @since 0.6.4
     */
    @Nullable
    static Object unmarshall(@NotNull EntityMetadata<?> entityMetadata, @Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        if (value != null && Objects.requireNonNull(value).getClass().isEnum()) {
            return value.toString().toLowerCase();
        } else if (unmarshallerMap.containsKey(entityMetadata.entityClass())) {
            if (unmarshallerMap.get(entityMetadata.entityClass()).containsKey(type)) {
                return unmarshallerMap.get(entityMetadata.entityClass()).get(type).marshall(value);
            }
        } else if (unmarshallerMap.containsKey(null)) {
            if (unmarshallerMap.get(null).containsKey(type)) {
                return unmarshallerMap.get(null).get(type).marshall(value);
            }
        }
        return value;
    }

    /**
     * @param entityMetadata
     * @param value
     * @param type
     * @param marshallerMap
     *
     * @return
     *
     * @since 0.6.4
     */
    @Nullable
    static Object marshall(@NotNull EntityMetadata<?> entityMetadata, @Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap) {
        if (type.isEnum() && value instanceof String) {
            try {
                var method = type.getMethod("valueOf", String.class);
                method.setAccessible(true);
                return method.invoke(null, ((String) value).toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
                return value;
            }
        } else if (marshallerMap.containsKey(entityMetadata.entityClass())) {
            if (marshallerMap.get(entityMetadata.entityClass()).containsKey(type)) {
                return marshallerMap.get(entityMetadata.entityClass()).get(type).marshall(value);
            }
        } else if (marshallerMap.containsKey(null)) {
            if (marshallerMap.get(null).containsKey(type)) {
                return marshallerMap.get(null).get(type).marshall(value);
            }
        }
        return value;
    }

}
