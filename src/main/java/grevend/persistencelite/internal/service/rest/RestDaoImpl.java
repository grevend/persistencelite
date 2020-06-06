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

import com.google.gson.Gson;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.internal.entity.representation.EntityDeserializer;
import grevend.persistencelite.internal.entity.representation.EntitySerializer;
import grevend.persistencelite.util.TypeMarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @since 0.4.7
 */
public final class RestDaoImpl implements DaoImpl<IOException> {

    private final EntityMetadata<?> entityMetadata;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap;

    @Contract(pure = true)
    public RestDaoImpl(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        this.entityMetadata = entityMetadata;
        this.marshallerMap = marshallerMap;
        this.unmarshallerMap = unmarshallerMap;
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws IOException {

    }

    @NotNull
    @Contract(" -> new")
    private Reader request() throws IOException {
        var con = new URL("http://localhost:8000/api/v2/country").openConnection();
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/pl.v0.entity+json; utf-8");
        return new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
    }

    @NotNull
    @Override
    public Iterable<Map<String, Object>> retrieve(@NotNull Iterable<String> keys, @NotNull Map<String, Object> props) throws IOException {
        try {
            return new Gson().fromJson(this.request(), EntityRequestResponse.class).entities
                .stream().map(entity -> this.entityMetadata.uniqueProperties().stream().map(
                    prop -> new SimpleEntry<>(prop.fieldName(), unmarshall(this.entityMetadata,
                        entity.props.containsKey(prop.fieldName()) ? entity.props
                            .get(prop.fieldName())
                            : (entity.props.getOrDefault(prop.propertyName(), null)),
                        prop.type(), this.unmarshallerMap)))
                    .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                        (olvV, newV) -> newV)))
                .collect(Collectors.toUnmodifiableList());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void update(@NotNull Iterable<Map<String, Object>> entity, @NotNull Map<String, Object> props) throws IOException {

    }

    @Override
    public void delete(@NotNull Map<String, Object> props) throws IOException {

    }

    @Nullable
    private static Object unmarshall(@NotNull EntityMetadata<?> entityMetadata, @Nullable Object value, @NotNull Class<?> type, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
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

    public static class EntityRequestResponse {

        public Map<String, String> types;
        public Collection<ResponseEntity> entities;

        @Override
        public String toString() {
            return "EntityRequestResponse{" +
                "types=" + types +
                ", entities=" + entities +
                '}';
        }
        
    }

    public static class ResponseEntity {

        public int type;
        public Map<String, String> props;
        public Map<String, String> rels;

        @Override
        public String toString() {
            return "ResponseEntity{" +
                "type=" + type +
                ", props=" + props +
                ", rels=" + rels +
                '}';
        }

    }

}
