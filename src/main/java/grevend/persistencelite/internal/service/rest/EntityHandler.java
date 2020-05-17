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
import grevend.persistencelite.dao.Dao;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.BaseDao;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.service.Service;
import grevend.sequence.Seq;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final record EntityHandler(@NotNull Service<?>service) implements RestHandler {

    @Override
    public Pair<Integer, String> handle(@NotNull URI uri, @NotNull String method, @NotNull Map<String, List<String>> query, int version, @NotNull EntityMetadata<?> entityMetadata) throws IOException {
        return switch (method) {
            case GET -> this.handleGet(query, entityMetadata);
            case POST -> this.handlePost(query, entityMetadata);
            case PATCH -> this.handlePatch(query, entityMetadata);
            case DELETE -> this.handleDelete(query, entityMetadata);
            default -> this.handleFailure(METHOD_NOT_ALLOWED, "Unexpected method " + method + ".");
        };
    }

    @NotNull
    private Pair<Integer, String> handleGet(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata) {
        Map<String, Object> props = Seq.of(query.entrySet())
            .filter(param -> !param.getValue().isEmpty())
            .map(param -> new PairImpl<>(param.getKey(), param.getValue().get(0))).collect(
                Collectors.toUnmodifiableMap(Pair::first, Pair::second,
                    (oldValue, newValue) -> newValue));

        try {
            Iterator<Map<String, Object>> res = this.daoImpl(entityMetadata)
                .retrieve(props.keySet(), props).iterator();

            var builder = new StringBuilder();
            builder.append("{\"entities\": [");

            builder.append(Seq.of(res).map(map -> "{" + Seq.of(map.entrySet()).map(entry -> "\"" + entry.getKey() + "\": \"" + entry.getValue() + "\"").joining(", ") + "}").joining(", "));

            /*res.forEachRemaining(map -> {
                builder.append("{");
                map.forEach((key, value) -> builder.append("\"").append(key).append("\": \"")
                    .append(value).append("\""));
                builder.append("}");
            });*/

            builder.append("]}");
            return this.handleSuccess(OK, builder.toString());
        } catch (Throwable throwable) {
            return this.handleFailure(NOT_FOUND, throwable.getMessage());
        }

        /*String response =
            "{\"persistencelite\": \""
                + PersistenceLite.VERSION
                + "\", \"entity\": \"" + entityMetadata.name()
                + "\", \"message\": \"Hello World!\"}";

        return this.handleSuccess(OK, response);*/
    }

    @NotNull
    private Pair<Integer, String> handlePost(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata) {
        return this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
    }

    @NotNull
    private Pair<Integer, String> handlePatch(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata) {
        return this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
    }

    @NotNull
    private Pair<Integer, String> handleDelete(@NotNull Map<String, List<String>> query, @NotNull EntityMetadata<?> entityMetadata) {
        return this.handleFailure(NOT_IMPLEMENTED, "Not implemented yet.");
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

    @NotNull
    private <E> Dao<E> dao(@NotNull EntityMetadata<E> entityMetadata) throws Throwable {
        return this.service.daoFactory()
            .createDao(entityMetadata, this.service.transactionFactory().createTransaction());
    }

    private record PairImpl<A, B>(A first, B second) implements Pair<A, B> {}

}
