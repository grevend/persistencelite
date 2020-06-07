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
import grevend.persistencelite.PersistenceLite;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.internal.entity.representation.EntityDeserializer;
import grevend.persistencelite.internal.entity.representation.EntitySerializer;
import grevend.persistencelite.util.TypeMarshaller;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import org.postgresql.core.ResultHandler;

/**
 * @author David Greven
 * @since 0.4.7
 */
public final class RestDaoImpl implements DaoImpl<IOException> {

    private final EntityMetadata<?> entityMetadata;
    private final String baseUrl;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap;

    @Contract(pure = true)
    public RestDaoImpl(@NotNull EntityMetadata<?> entityMetadata, @NotNull String baseUrl, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        this.entityMetadata = entityMetadata;
        this.baseUrl = baseUrl;
        this.marshallerMap = marshallerMap;
        this.unmarshallerMap = unmarshallerMap;
        HttpURLConnection.setFollowRedirects(false);
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws IOException {

    }

    private HttpURLConnection connection() throws IOException {
        return (HttpURLConnection) new URL(this.baseUrl + this.entityMetadata.name().toLowerCase())
            .openConnection();
    }

    @NotNull
    @Contract(" -> new")
    private Reader request() throws IOException {
        var con = this.connection();
        con.setRequestMethod("GET");
        con.setDoOutput(false);
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/pl.v0.entity+json; utf-8");
        con.setRequestProperty("User-Agent", "Java/" + Runtime.version().feature() +
            " PersistenceLite/" + PersistenceLite.VERSION);
        con.setRequestProperty("Transfer-Encoding", "chunked");
        return new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
    }

    record Res(Writer writer, HttpURLConnection connection) {}

    @NotNull
    @Contract("_ -> new")
    private Res requestWithBody(@NotNull String method) throws IOException {
        var con = this.connection();
        if(method.equals(RestHandler.PATCH)) {
            con.setRequestProperty("X-HTTP-Method-Override", RestHandler.PATCH);
            con.setRequestMethod(RestHandler.POST);
        } else {
            con.setRequestMethod(method);
        }
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/pl.v0.entity+json; utf-8");
        con.setRequestProperty("User-Agent", "Java/" + Runtime.version().feature() +
            " PersistenceLite/" + PersistenceLite.VERSION);
        con.setRequestProperty("Transfer-Encoding", "chunked");
        con.setDoOutput(true);
        con.setDoInput(true);
        return new Res(new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8), con);
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
        var request = this.requestWithBody(RestHandler.PATCH);
        var writer = request.writer;
        var entityIter = entity.iterator();
        writer.write("{\"entity\": [");
        while (entityIter.hasNext()) {
            writer.flush();
            var next = entityIter.next();
            writer.write("{");
            var entries = next.entrySet().iterator();
            while(entries.hasNext()) {
                var entry = entries.next();
                writer.flush();
                writer.write("\"" + entry.getKey() + "\": \"" + entry.getValue() +
                    "\"" + (entries.hasNext() ? " ," : ""));
            }
            writer.write("}" + (entityIter.hasNext() ? ", " : ""));
        }
        writer.flush();
        writer.write("], \"props\": {");
        //TODO
        writer.write("}}");
        writer.flush();
        writer.close();
        System.out.println(request.connection.getResponseCode());
    }

    @Override
    public void delete(@NotNull Map<String, Object> props) throws IOException {
        var writer = this.requestWithBody(RestHandler.DELETE).writer;
        writer.flush();
        writer.close();
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
