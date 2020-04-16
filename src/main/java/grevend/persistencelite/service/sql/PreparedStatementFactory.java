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

package grevend.persistencelite.service.sql;

import grevend.persistencelite.entity.EntityMetadata;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

final class PreparedStatementFactory {

    @NotNull
    PreparedStatement prepare(@NotNull StatementType statementType, @NotNull Connection connection, @NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) throws SQLException {
        var cache = StatementCache.getInstance().getStatementMap();
        if (!cache.containsKey(entityMetadata)) {
            cache.put(entityMetadata, new HashMap<>());
        }
        if (!cache.get(entityMetadata).containsKey(statementType)) {
            cache.get(entityMetadata).put(statementType, switch (statementType) {
                case INSERT -> this.prepareInsert(entityMetadata, properties);
                case SELECT -> this.prepareSelect(entityMetadata, properties);
                case SELECT_ALL -> this.prepareSelectAll(entityMetadata);
                case UPDATE -> null;
                case DELETE -> this.prepareDelete(entityMetadata, properties);
            });
        }
        return connection.prepareStatement(
            StatementCache.getInstance().getStatementMap().get(entityMetadata).get(statementType));
    }

    @NotNull
    private String prepareInsert(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) {
        return "insert into " + entityMetadata.getName() + " (" + String
            .join(", ", properties.keySet()) + ") values (" + String
            .join(", ", Collections.nCopies(properties.keySet().size(), "?")) + ")";
    }

    @NotNull
    private String prepareSelect(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) {
        return "select * from " + entityMetadata.getName() + " where " + properties.keySet()
            .stream().map(property -> property + "=?").collect(Collectors.joining(", "));
    }

    @NotNull
    private String prepareSelectAll(@NotNull EntityMetadata<?> entityMetadata) {
        return "select * from " + entityMetadata.getName();
    }

    @NotNull
    private String prepareDelete(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) {
        return "delete from " + entityMetadata.getName() + " where " + properties.keySet().stream()
            .map(attribute -> attribute + "=?").collect(Collectors.joining(", "));
    }

    enum StatementType {
        INSERT, SELECT, SELECT_ALL, UPDATE, DELETE
    }

    static final class StatementCache {

        private static final Object MUTEX = new Object();
        private static volatile StatementCache INSTANCE;
        private final Map<EntityMetadata<?>, Map<StatementType, String>> preparedStatementMap;

        @Contract(pure = true)
        private StatementCache() {
            this.preparedStatementMap = new HashMap<>();
        }

        @NotNull
        private static StatementCache getInstance() {
            var result = INSTANCE;
            if (result == null) {
                synchronized (MUTEX) {
                    result = INSTANCE;
                    if (result == null) {
                        INSTANCE = result = new StatementCache();
                    }
                }
            }
            return result;
        }

        @NotNull
        @Contract(pure = true)
        private Map<EntityMetadata<?>, Map<StatementType, String>> getStatementMap() {
            return this.preparedStatementMap;
        }

        @SuppressWarnings("unused")
        void clearCache() {
            this.preparedStatementMap.clear();
        }

    }

}
