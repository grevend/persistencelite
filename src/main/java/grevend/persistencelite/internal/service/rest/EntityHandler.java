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

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import grevend.common.Pair;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.BaseDao;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.internal.util.Utils;
import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.Seq;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final record EntityHandler(@NotNull RestConfiguration configuration) implements RestHandler {

    public static final ConcurrentHashMap<EntityMetadata<?>, ZonedDateTime> lastModified = new ConcurrentHashMap<>();

    public void handle(int version, @NotNull EntityMetadata<?> entityMetadata,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap, @NotNull HttpExchange exchange) {
        try {
            var method = exchange.getRequestMethod();
            var props = this.extractProps(Utils.query(exchange.getRequestURI()), entityMetadata,
                unmarshallerMap);
            switch (exchange.getRequestHeaders().containsKey("X-http-method-override") ? (exchange
                .getRequestHeaders().getFirst("X-http-method-override").toUpperCase()) : method) {
                case HEAD -> this.handleHead(exchange);
                case GET -> this.handleGet(version, props, entityMetadata, marshallerMap,
                    unmarshallerMap, exchange);
                case POST, PUT -> this.handlePut(entityMetadata, exchange, marshallerMap,
                    unmarshallerMap);
                case PATCH -> this.handlePatch(entityMetadata, exchange, unmarshallerMap);
                case DELETE -> this.handleDelete(props, entityMetadata, exchange, unmarshallerMap);
                default -> exchange.sendResponseHeaders(NOT_IMPLEMENTED, 0);
            }
        } catch (Throwable throwable) {
            try {
                throwable.printStackTrace();
                exchange.sendResponseHeaders(NOT_FOUND, 0);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    @NotNull
    private Map<String, Object> extractProps(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        record PairImpl<A, B>(A first, B second) implements Pair<A, B> {}

        var properties = entityMetadata.declaredProperties();

        Map<String, String> props = Seq.of(query.entrySet())
            .filter(param -> !param.getValue().isEmpty())
            .map(param -> new PairImpl<>(param.getKey(), param.getValue().get(0)))
            .collect(Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                (oldValue, newValue) -> newValue));

        return Seq.of(properties)
            .filter(prop -> props.containsKey(prop.fieldName()))
            .map(prop -> new PairImpl<>(prop.fieldName(), unmarshall(entityMetadata,
                props.get(prop.fieldName()), prop.type(), unmarshallerMap)))
            .collect(Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                (oldValue, newValue) -> newValue));
    }

    private boolean isProprietary(@NotNull HttpExchange exchange) {
        return exchange.getRequestHeaders().containsKey("User-Agent") &&
            exchange.getRequestHeaders().getFirst("User-Agent")
                .contains("PersistenceLite") &&
            exchange.getRequestHeaders().getFirst("User-Agent")
                .contains("MagicNumber/32204d61722032303230");
    }

    private Map<String, Class<?>> getTypes(@NotNull EntityMetadata<?> entityMetadata) {
        return entityMetadata.properties().stream()
            .map(prop -> new SimpleEntry<>(prop.fieldName(), prop.type()))
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                (oldV, newV) -> newV));
    }

    private void handleHead(@NotNull HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(OK, 0);
    }

    private void handleGet(int version, @NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap, @NotNull HttpExchange exchange) throws IOException {
        try {
            var types = this.getTypes(entityMetadata);

            if (this.isProprietary(exchange)) {
                props = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody()),
                    Props.class).props.entrySet().stream().map(prop -> {
                    try {
                        return new SimpleEntry<>(prop.getKey(), unmarshall(entityMetadata,
                            prop.getValue(), types.get(prop.getKey()), unmarshallerMap));
                    } catch (Throwable throwable) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                        (oldV, newV) -> newV));
            }

            var relations = entityMetadata.declaredRelations();
            var entities = this.daoImpl(entityMetadata).retrieve(props.keySet(), props).iterator();
            exchange.sendResponseHeaders(OK, CHUNKED);
            var out = exchange.getResponseBody();
            out.write(("{\"types\": {\"0\": \"" + entityMetadata.name() + "\"}, \"entities\": [")
                .getBytes(this.configuration.charset()));
            while (entities.hasNext()) {
                out.flush();
                out.write("{\"type\": 0, \"props\": {".getBytes(this.configuration.charset()));

                var properties = Seq.of(entities.next().entrySet())
                    .filter(entry -> relations.stream()
                        .noneMatch(rel -> rel.propertyName().equals(entry.getKey())))
                    .map(entry -> "\"" + entry.getKey() + "\": \"" + marshall(entityMetadata,
                        entry.getValue(), types.get(entry.getKey()), marshallerMap) + "\"")
                    .iterator();

                while (properties.hasNext()) {
                    out.flush();
                    out.write((properties.next() + (properties.hasNext() ? ", " : ""))
                        .getBytes(this.configuration.charset()));
                }

                out.flush();
                out.write("}, \"rels\": {".getBytes(this.configuration.charset()));

                /*for (var current : relations) {
                    out.flush();
                    out.write(("\"" + current.propertyName() + "\": \"http://localhost:8000/api/v"
                        + version + "/" + Objects.requireNonNull(current.relation())
                        .getTargetEntity().getSimpleName().toLowerCase() + "?")
                        .getBytes(this.configuration.charset()));
                    out.flush();

                    var params = Utils.zip(Seq.of(Objects.requireNonNull(current.relation())
                            .getTargetProperties()).iterator(),
                        Seq.of(Objects.requireNonNull(current.relation())
                            .getSelfProperties()).iterator()).iterator();

                    while (params.hasNext()) {
                        var param = params.next();
                        out.flush();
                        out.write((param.first() + "=").getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        out.write((props.get(param.second()).toString() ("test" +
                            (params.hasNext() ? "&" : "")).getBytes(StandardCharsets.UTF_8));
                        out.write((param.first() + "=" + props.get(param.second()).toString() +
                            (params.hasNext() ? "&" : "")).getBytes(StandardCharsets.UTF_8));
                    }

                    out.flush();
                    out.write("\"".getBytes(this.configuration.charset()));
                }*/

                out.flush();
                out.write(("}}" + (entities.hasNext() ? ", " : ""))
                    .getBytes(this.configuration.charset()));
                out.flush();
            }
            out.write("]}".getBytes(this.configuration.charset()));
            out.flush();
            out.close();
        } catch (Throwable throwable) {
            exchange.sendResponseHeaders(NOT_FOUND, 0);
            throwable.printStackTrace();
        }
    }

    private void handlePut(@NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) throws IOException {
        try {
            var request = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody()),
                Entity.class);
            var res = request.entity.stream().map(input ->
                this.unmarshallMap(input, entityMetadata, unmarshallerMap))
                .collect(Collectors.toList());
            this.daoImpl(entityMetadata).create(res);

            EntityHandler.lastModified.put(entityMetadata, ZonedDateTime.now());

            var resIter = res.iterator();
            var types = this.getTypes(entityMetadata);

            exchange.sendResponseHeaders(CREATED, CHUNKED);

            var out = exchange.getResponseBody();
            out.write("{\"entity\": [".getBytes(this.configuration.charset()));
            while (resIter.hasNext()) {
                out.flush();
                out.write("{".getBytes(this.configuration.charset()));

                var props = resIter.next();
                var entryIter = props.entrySet().iterator();

                while (entryIter.hasNext()) {
                    out.flush();
                    var entry = entryIter.next();
                    out.write(("\"" + entry.getKey() + "\": \"" + marshall(entityMetadata,
                        entry.getValue(), types.get(entry.getKey()), marshallerMap) + "\"")
                        .getBytes(this.configuration.charset()));
                    if (entryIter.hasNext()) {
                        out.write(", ".getBytes(this.configuration.charset()));
                    }
                }

                out.write(("}" + (resIter.hasNext() ? ", " : ""))
                    .getBytes(this.configuration.charset()));
            }
            out.write("]}".getBytes(this.configuration.charset()));
            out.flush();
            out.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            exchange.sendResponseHeaders(BAD_REQUEST, 0);
        }
    }

    @NotNull
    private Map<String, Object> unmarshallMap(@NotNull Map<String, String> input, @NotNull EntityMetadata<?> entityMetadata,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) {
        var types = this.getTypes(entityMetadata);
        return input.entrySet().stream().map(entry -> {
            try {
                return new SimpleEntry<>(entry.getKey(), unmarshall(entityMetadata,
                    entry.getValue(), types.get(entry.getKey()), unmarshallerMap));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toMap(Entry::getKey, Entry::getValue,
            (oldV, newV) -> newV));
    }

    private void handlePatch(@NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) throws IOException {
        try {
            var request = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody()),
                EntityProps.class);
            this.daoImpl(entityMetadata).update(request.entity.stream().map(input ->
                    this.unmarshallMap(input, entityMetadata, unmarshallerMap))
                    .collect(Collectors.toList()),
                this.unmarshallMap(request.props, entityMetadata, unmarshallerMap));
            EntityHandler.lastModified.put(entityMetadata, ZonedDateTime.now());
            exchange.sendResponseHeaders(OK, 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            exchange.sendResponseHeaders(BAD_REQUEST, 0);
        }
    }

    private void handleDelete(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata,
        @NotNull HttpExchange exchange, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> unmarshallerMap) throws IOException {
        try {
            if (this.isProprietary(exchange)) {
                var request = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody()),
                    Props.class);
                this.daoImpl(entityMetadata).delete(this.unmarshallMap(request.props,
                    entityMetadata, unmarshallerMap));
            } else {
                this.daoImpl(entityMetadata).delete(props);
            }
            EntityHandler.lastModified.put(entityMetadata, ZonedDateTime.now());
            exchange.sendResponseHeaders(OK, 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            exchange.sendResponseHeaders(BAD_REQUEST, 0);
        }
    }

    @NotNull
    private DaoImpl<?> daoImpl(@NotNull EntityMetadata<?> entityMetadata) throws Throwable {
        if (Objects.requireNonNull(this.configuration.service()).daoFactory()
            .createDao(entityMetadata, Objects.requireNonNull(this.configuration.service())
                .transactionFactory().createTransaction()) instanceof BaseDao<?, ?> baseDao) {
            return baseDao.daoImpl();
        } else {
            throw new IllegalStateException("Failed to construct a DaoImpl.");
        }
    }

    private static final class Props {

        public Map<String, String> props;

        @Override
        public String toString() {
            return "Props{" +
                "props=" + this.props +
                '}';
        }

    }

    private static final class Entity {

        public Collection<Map<String, String>> entity;

        @Override
        public String toString() {
            return "Entity{" +
                "entity=" + this.entity +
                '}';
        }

    }

    private static final class EntityProps {

        public Collection<Map<String, String>> entity;
        public Map<String, String> props;

        @Override
        public String toString() {
            return "EntityProps{" +
                "entity=" + this.entity +
                ", props=" + this.props +
                '}';
        }

    }

}
