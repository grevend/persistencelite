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
                marshallerMap);
            switch (exchange.getRequestHeaders().containsKey("X-http-method-override") ? (exchange
                .getRequestHeaders().getFirst("X-http-method-override").toUpperCase()) : method) {
                case HEAD -> this.handleHead(exchange);
                case GET -> this.handleGet(version, props, entityMetadata, marshallerMap, exchange);
                case POST, PUT -> this.handlePut(entityMetadata, exchange);
                case PATCH -> this.handlePatch(entityMetadata, exchange);
                case DELETE -> this.handleDelete(props, entityMetadata, exchange);
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
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap) {
        record PairImpl<A, B>(A first, B second) implements Pair<A, B> {}

        var properties = entityMetadata.declaredProperties();

        Map<String, String> props = Seq.of(query.entrySet())
            .filter(param -> !param.getValue().isEmpty())
            .map(param -> new PairImpl<>(param.getKey(), param.getValue().get(0)))
            .collect(Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                (oldValue, newValue) -> newValue));

        return Seq.of(properties)
            .filter(prop -> props.containsKey(prop.fieldName()))
            .map(prop -> new PairImpl<>(prop.fieldName(),
                marshall(entityMetadata, props.get(prop.fieldName()), prop.type(), marshallerMap)))
            .collect(Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                (oldValue, newValue) -> newValue));
    }

    private boolean isProprietary(@NotNull HttpExchange exchange) {
        return exchange.getRequestHeaders().containsKey("X-http-method-override") &&
            exchange.getRequestHeaders().containsKey("User-Agent") &&
            exchange.getRequestHeaders().getFirst("X-http-method-override").equals("GET") &&
            exchange.getRequestHeaders().getFirst("User-Agent").contains("PersistenceLite");
    }

    private void handleHead(@NotNull HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(OK, 0);
    }

    private void handleGet(int version, @NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata,
        @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>> marshallerMap, @NotNull HttpExchange exchange) throws IOException {
        try {
            var types = entityMetadata.properties().stream()
                .map(prop -> new SimpleEntry<>(prop.fieldName(), prop.type()))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                    (oldV, newV) -> newV));

            System.out.println("Types: " + types);

            if (this.isProprietary(exchange)) {
                props = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody()),
                    Props.class).props.entrySet().stream().map(prop -> {
                    try {
                        return new SimpleEntry<>(prop.getKey(),
                            marshall(entityMetadata, prop.getValue(), types.get(prop.getKey()),
                                marshallerMap));
                    } catch (Throwable throwable) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                        (oldV, newV) -> newV));
            }
            System.out.println("Props: " + props);

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
                    .map(entry -> "\"" + entry.getKey() + "\": " + (
                        entry.getValue() instanceof Number num ? num
                            : ("\"" + entry.getValue() + "\""))).iterator();

                while (properties.hasNext()) {
                    out.flush();
                    out.write((properties.next() + (properties.hasNext() ? ", " : ""))
                        .getBytes(this.configuration.charset()));
                }

                out.flush();
                out.write("}, \"rels\": {".getBytes(this.configuration.charset()));

                for (var current : relations) {
                    out.flush();
                    out.write(("\"" + current.propertyName() + "\": \"http://localhost:8000/api/v"
                        + version + "/" + Objects.requireNonNull(current.relation())
                        .getTargetEntity().getSimpleName().toLowerCase() + "?")
                        .getBytes(this.configuration.charset()));
                    out.flush();

                    /*var params = Utils.zip(Seq.of(Objects.requireNonNull(current.relation())
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
                    }*/

                    out.flush();
                    out.write("\"".getBytes(this.configuration.charset()));
                }

                out.flush();
                out.write(("}}" + (entities.hasNext() ? ", " : ""))
                    .getBytes(this.configuration.charset()));
                out.flush();
            }
            out.write("]}".getBytes(this.configuration.charset()));
            out.flush();
            out.close();
        } catch (Throwable throwable) {
            exchange.sendResponseHeaders(NOT_FOUND, throwable.getMessage().length());
        }
    }

    private void handlePut(@NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange) throws IOException {
        /*System.out.println("PUT: " + new Gson().fromJson(new InputStreamReader(
            exchange.getRequestBody()), Entity.class));*/

        exchange.sendResponseHeaders(NOT_IMPLEMENTED, 0);
    }

    private void handlePatch(@NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange) throws IOException {
        /*System.out.println("PATCH: " + new Gson().fromJson(new InputStreamReader(
            exchange.getRequestBody()), EntityProps.class));*/

        exchange.sendResponseHeaders(NOT_IMPLEMENTED, 0);
    }

    private void handleDelete(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange) throws IOException {
        try {
            if (this.isProprietary(exchange)) {
                /*System.out.println("DELETE: " + new Gson().fromJson(new InputStreamReader(
                    exchange.getRequestBody()), Props.class));*/

                exchange.sendResponseHeaders(NOT_IMPLEMENTED, 0);
            } else {
                this.daoImpl(entityMetadata).delete(props);
                exchange.sendResponseHeaders(OK, 0);
            }
        } catch (Throwable throwable) {
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

    }

    private static final class Entity {

        public Collection<Map<String, String>> entities;

    }

    private static final class EntityProps {

        public Collection<Map<String, String>> entities;
        public Map<String, String> props;

    }

}
