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
import grevend.persistencelite.util.TypeMarshaller;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @since 0.6.4
 */
final class RestUtils {

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

    @Nullable
    private static Object marshall(@NotNull EntityMetadata<?> entityMetadata, @Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap) {
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
