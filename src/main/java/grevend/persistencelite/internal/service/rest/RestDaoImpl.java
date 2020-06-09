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

import static grevend.persistencelite.internal.service.rest.RestUtils.marshall;
import static grevend.persistencelite.internal.service.rest.RestUtils.unmarshall;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import grevend.persistencelite.PersistenceLite;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.Seq;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @since 0.4.7
 */
public final class RestDaoImpl implements DaoImpl<IOException> {

    private final EntityMetadata<?> entityMetadata;
    private final String baseUrl;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap;
    private final Map<String, Class<?>> entityTypes;
    ZonedDateTime lastModified;

    @Contract(pure = true)
    public RestDaoImpl(@NotNull EntityMetadata<?> entityMetadata, @NotNull String baseUrl, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        this.entityMetadata = entityMetadata;
        this.baseUrl = baseUrl;
        this.marshallerMap = marshallerMap;
        this.unmarshallerMap = unmarshallerMap;
        HttpURLConnection.setFollowRedirects(false);

        this.entityTypes = this.entityMetadata.properties().stream()
            .map(prop -> new SimpleEntry<>(prop.fieldName(), prop.type()))
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                (oldV, newV) -> newV));
    }

    HttpURLConnection connection() throws IOException {
        return (HttpURLConnection) new URL(this.baseUrl +
            this.entityMetadata.name().toLowerCase()).openConnection();
    }

    @NotNull
    @Contract(" -> new")
    private HttpURLConnection request() throws IOException {
        var conn = this.connection();
        conn.setRequestProperty("X-HTTP-Method-Override", RestHandler.GET);
        conn.setRequestMethod(RestHandler.POST);
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("Content-Type", "application/pl.v0.entity+json; utf-8");
        conn.setRequestProperty("User-Agent", "PersistenceLite/" + PersistenceLite.VERSION +
            " (Java/" + Runtime.version() + ")");
        conn.setRequestProperty("Transfer-Encoding", "chunked");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        return conn;
    }

    @NotNull
    @Contract("_ -> new")
    private RequestUnidirectional requestWithBody(@NotNull String method) throws IOException {
        var conn = this.connection();
        if (method.equals(RestHandler.PATCH)) {
            conn.setRequestProperty("X-HTTP-Method-Override", RestHandler.PATCH);
            conn.setRequestMethod(RestHandler.POST);
        } else {
            conn.setRequestMethod(method);
        }
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("Content-Type", "application/pl.v0.entity+json; utf-8");
        conn.setRequestProperty("User-Agent", "PersistenceLite/" + PersistenceLite.VERSION +
            " (Java/" + Runtime.version() + ")");
        conn.setRequestProperty("Transfer-Encoding", "chunked");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        return new RequestUnidirectional(new OutputStreamWriter(conn.getOutputStream(), UTF_8),
            conn);
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws IOException {
        var request = this.requestWithBody(RestHandler.PUT);
        var writer = request.writer;
        var entityIter = entity.iterator();
        writer.write("{\"entity\": [");
        while (entityIter.hasNext()) {
            writer.flush();
            var next = entityIter.next();
            writer.write("{");
            var entries = next.entrySet().iterator();
            while (entries.hasNext()) {
                var entry = entries.next();
                writer.flush();
                writer.write("\"" + entry.getKey() + "\": \"" +
                    (this.entityTypes.containsKey(entry.getKey()) ? marshall(this.entityMetadata,
                        entry.getValue(), this.entityTypes.get(entry.getKey()), this.marshallerMap)
                        : null) + "\"" + (entries.hasNext() ? ", " : ""));
            }
            writer.write("}" + (entityIter.hasNext() ? ", " : ""));
        }
        writer.write("]}");
        writer.flush();
        writer.close();
        if (request.connection.getResponseCode() != RestHandler.OK &&
            request.connection.getResponseCode() != RestHandler.CREATED) {
            throw new IllegalStateException("Server responded with error code <" +
                request.connection.getResponseCode() + ">.");
        } else {
            /*
            this.lastModified = ZonedDateTime.parse(request.getHeaderField("Last-Modified"),
                DateTimeFormatter.RFC_1123_DATE_TIME);
             */
        }
    }

    @NotNull
    @Override
    public Iterable<Map<String, Object>> retrieve(@NotNull Iterable<String> keys, @NotNull Map<String, Object> props) throws IOException {
        try {
            var request = this.request();
            var writer = new OutputStreamWriter(request.getOutputStream(), UTF_8);
            writer.write("{\"props\": {");
            var propIter = props.entrySet().iterator();
            while (propIter.hasNext()) {
                writer.flush();
                var entry = propIter.next();
                if (Seq.of(keys).anyMatch(key -> Objects.equals(key, entry.getKey()))) {
                    writer.write("\"" + entry.getKey() + "\": \"" +
                        (this.entityTypes.containsKey(entry.getKey()) ?
                            marshall(this.entityMetadata, entry.getValue(),
                                this.entityTypes.get(entry.getKey()), this.marshallerMap)
                            : null) + "\"" + (propIter.hasNext() ? ", " : ""));
                }

            }
            writer.write("}}");
            writer.flush();
            writer.close();

            var res = new Gson().fromJson(new InputStreamReader(request.getInputStream(), UTF_8),
                EntityRequestResponse.class).entities.stream().map(entity ->
                this.entityMetadata.uniqueProperties().stream().map(prop ->
                    new SimpleEntry<>(prop.fieldName(), unmarshall(this.entityMetadata,
                        entity.props.containsKey(prop.fieldName()) ? entity.props
                            .get(prop.fieldName())
                            : (entity.props.getOrDefault(prop.propertyName(), null)),
                        prop.type(), this.unmarshallerMap)))
                    .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                        (olvV, newV) -> newV)))
                .collect(Collectors.toUnmodifiableList());

            this.lastModified = ZonedDateTime.parse(request.getHeaderField("Last-Modified"),
                DateTimeFormatter.RFC_1123_DATE_TIME);

            return res;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Request to server failed.", throwable);
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
            while (entries.hasNext()) {
                writer.flush();
                var entry = entries.next();
                writer.write("\"" + entry.getKey() + "\": \"" +
                    (this.entityTypes.containsKey(entry.getKey()) ? marshall(this.entityMetadata,
                        entry.getValue(), this.entityTypes.get(entry.getKey()), this.marshallerMap)
                        : null) + "\"" + (entries.hasNext() ? " ," : ""));
            }
            writer.write("}" + (entityIter.hasNext() ? ", " : ""));
        }
        writer.write("], \"props\": {");
        var propIter = props.entrySet().iterator();
        while (propIter.hasNext()) {
            writer.flush();
            var entry = propIter.next();
            writer.write("\"" + entry.getKey() + "\": \"" +
                (this.entityTypes.containsKey(entry.getKey()) ? marshall(this.entityMetadata,
                    entry.getValue(), this.entityTypes.get(entry.getKey()), this.marshallerMap)
                    : null) + "\"" + (propIter.hasNext() ? ", " : ""));
        }
        writer.write("}}");
        writer.flush();
        writer.close();
        if (request.connection.getResponseCode() != RestHandler.OK) {
            throw new IllegalStateException("Server responded with error code <" +
                request.connection.getResponseCode() + ">.");
        } else {
            /*
            this.lastModified = ZonedDateTime.parse(request.getHeaderField("Last-Modified"),
                DateTimeFormatter.RFC_1123_DATE_TIME);
             */
        }
    }

    @Override
    public void delete(@NotNull Map<String, Object> props) throws IOException {
        var request = this.requestWithBody(RestHandler.DELETE);
        var writer = request.writer;
        writer.write("{\"props\": {");
        var propIter = props.entrySet().iterator();
        while (propIter.hasNext()) {
            writer.flush();
            var entry = propIter.next();
            writer.write("\"" + entry.getKey() + "\": \"" +
                (this.entityTypes.containsKey(entry.getKey()) ? marshall(this.entityMetadata,
                    entry.getValue(), this.entityTypes.get(entry.getKey()), this.marshallerMap)
                    : null) + "\"" + (propIter.hasNext() ? ", " : ""));
        }
        writer.write("}}");
        writer.flush();
        writer.close();
        if (request.connection.getResponseCode() != RestHandler.OK) {
            throw new IllegalStateException("Server responded with error code <" +
                request.connection.getResponseCode() + ">.");
        } else {
            /*
            this.lastModified = ZonedDateTime.parse(request.getHeaderField("Last-Modified"),
                DateTimeFormatter.RFC_1123_DATE_TIME);
             */
        }
    }

    private record RequestUnidirectional(Writer writer, HttpURLConnection connection) {}

    public static class EntityRequestResponse {

        public Map<String, String> types;
        public Collection<ResponseEntity> entities;

        @Override
        public String toString() {
            return "EntityRequestResponse{" +
                "types=" + this.types +
                ", entities=" + this.entities +
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
                "type=" + this.type +
                ", props=" + this.props +
                ", rels=" + this.rels +
                '}';
        }

    }

}
