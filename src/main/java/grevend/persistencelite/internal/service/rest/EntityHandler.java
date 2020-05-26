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

import com.sun.net.httpserver.HttpExchange;
import grevend.common.Pair;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.BaseDao;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.Seq;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final record EntityHandler(@NotNull RestConfiguration configuration) implements RestHandler {

    public static final ConcurrentHashMap<EntityMetadata<?>, ZonedDateTime> lastModified
        = new ConcurrentHashMap<>(9);

    private static final Map<Class<?>, TypeMarshaller<String, Object>> mappers = Map.of(
        String.class, val -> val,
        Integer.class, Integer::parseInt,
        Integer.TYPE, Integer::parseInt,
        Float.class, Float::parseFloat,
        Float.TYPE, Float::parseFloat
    );

    public void handle(@NotNull URI uri, @NotNull String method, @NotNull Map<String, List<String>> query, int version, @NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange) {
        try {
            var props = this.extractProps(query, entityMetadata);
            switch (method) {
                case GET -> this.handleGet(props, entityMetadata, exchange);
                case POST -> this.handlePost(props, entityMetadata);
                case PATCH -> this.handlePatch(props, entityMetadata);
                case DELETE -> this.handleDelete(props, entityMetadata);
                default -> this
                    .handleFailure(METHOD_NOT_ALLOWED, "Unexpected method " + method + ".");
            }
        } catch (Throwable throwable) {
            this.handleFailure(NOT_FOUND, throwable.getMessage());
        }
    }

    @NotNull
    private Map<String, Object> extractProps(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata) {
        var properties = entityMetadata.declaredProperties();

        Map<String, String> props = Seq.of(query.entrySet())
            .filter(param -> !param.getValue().isEmpty())
            .map(param -> new PairImpl<>(param.getKey(), param.getValue().get(0))).collect(
                Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                    (oldValue, newValue) -> newValue));

        return Seq.of(properties)
            .filter(prop -> props.containsKey(prop.fieldName()))
            .map(prop -> mappers.get(prop.type()) == null ? null
                : new PairImpl<>(prop.fieldName(),
                    mappers.get(prop.type()).marshall(props.get(prop.fieldName()))))
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                (oldValue, newValue) -> newValue));
    }

    private void handleGet(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata, @NotNull HttpExchange exchange) throws IOException {
        try {
            var relations = entityMetadata.declaredRelations();
            var entities = this.daoImpl(entityMetadata).retrieve(props.keySet(), props).iterator();
            exchange.sendResponseHeaders(OK, CHUNKED);
            var out = exchange.getResponseBody();
            //TODO Add charset configuration to REST service
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
                    out.write(("\"" + current.propertyName() + "\": \"http://localhost:8000/api/v0/"
                        + Objects.requireNonNull(current.relation()).getTargetEntity()
                        .getSimpleName().toLowerCase() + "?")
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

    private void handlePost(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
    }

    private void handlePatch(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
    }

    private void handleDelete(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        try {
            this.daoImpl(entityMetadata).delete(props);
            this.handleSuccess(OK, "Deletion successful.");
        } catch (Throwable throwable) {
            this.handleFailure(INTERNAL_SERVER_ERROR, "Not implemented yet.");
        }
    }

    private void handleFailure(@Range(from = 400, to = 501) int code, @NotNull String reason) {
        new PairImpl<>(code, "{\"message\": \"" + reason + "\"}");
    }

    private void handleSuccess(int code, @NotNull String json) {
        new PairImpl<>(code, json);
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

    private record PairImpl<A, B>(A first, B second) implements Pair<A, B> {}

}
