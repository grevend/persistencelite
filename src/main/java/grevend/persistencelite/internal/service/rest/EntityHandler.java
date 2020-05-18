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

import grevend.common.Pair;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.BaseDao;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.service.Service;
import grevend.persistencelite.util.TypeMarshaller;
import grevend.sequence.Seq;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final record EntityHandler(@NotNull Service<?>service) implements RestHandler {

    private static final Map<Class<?>, TypeMarshaller<String, Object>> mappers = Map.of(
        String.class, val -> val,
        Integer.class, Integer::parseInt,
        Integer.TYPE, Integer::parseInt,
        Float.class, Float::parseFloat,
        Float.TYPE, Float::parseFloat
    );

    @Override
    public Pair<Integer, String> handle(@NotNull URI uri, @NotNull String method, @NotNull Map<String, List<String>> query, int version, @NotNull EntityMetadata<?> entityMetadata) throws IOException {
        try {
            var props = this.extractProps(query, entityMetadata);
            return switch (method) {
                case GET -> this.handleGet(props, entityMetadata);
                case POST -> this.handlePost(props, entityMetadata);
                case PATCH -> this.handlePatch(props, entityMetadata);
                case DELETE -> this.handleDelete(props, entityMetadata);
                default -> this
                    .handleFailure(METHOD_NOT_ALLOWED, "Unexpected method " + method + ".");
            };
        } catch (Throwable throwable) {
            return this.handleFailure(NOT_FOUND, throwable.getMessage());
        }
    }

    @NotNull
    private Map<String, Object> extractProps(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata) throws Throwable {
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

    @NotNull
    private Pair<Integer, String> handleGet(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        try {
            return this.handleSuccess(OK, "{\"entities\": [" + Seq
                .of(this.daoImpl(entityMetadata).retrieve(props.keySet(), props).iterator()).map(
                    map -> "{" + Seq.of(map.entrySet())
                        .map(entry -> "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"")
                        .joining(", ") + "}").joining(", ") + "]}");
        } catch (Throwable throwable) {
            return this.handleFailure(NOT_FOUND, throwable.getMessage());
        }
    }

    @NotNull
    private Pair<Integer, String> handlePost(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        return this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
    }

    @NotNull
    private Pair<Integer, String> handlePatch(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        return this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
    }

    @NotNull
    private Pair<Integer, String> handleDelete(@NotNull Map<String, Object> props, @NotNull EntityMetadata<?> entityMetadata) {
        try {
            this.daoImpl(entityMetadata).delete(props);
            return this.handleSuccess(OK, "Deletion successful.");
        } catch (Throwable throwable) {
            return this.handleFailure(INTERNAL_SERVER_ERROR, "Not implemented yet.");
        }
    }

    @NotNull
    @Contract("_, _ -> new")
    private Pair<Integer, String> handleFailure(@Range(from = 400, to = 501) int code, @NotNull String reason) {
        return new PairImpl<>(code, "{\"message\": \"" + reason + "\"}");
    }

    @NotNull
    @Contract("_, _ -> new")
    private Pair<Integer, String> handleSuccess(int code, @NotNull String json) {
        return new PairImpl<>(code, json);
    }

    @NotNull
    private DaoImpl<?> daoImpl(@NotNull EntityMetadata<?> entityMetadata) throws Throwable {
        if (this.service.daoFactory().createDao(entityMetadata, this.service.transactionFactory()
            .createTransaction()) instanceof BaseDao<?, ?> baseDao) {
            return baseDao.daoImpl();
        } else {
            throw new IllegalStateException("Failed to construct a DaoImpl.");
        }
    }

    private record PairImpl<A, B>(A first, B second) implements Pair<A, B> {}

}
