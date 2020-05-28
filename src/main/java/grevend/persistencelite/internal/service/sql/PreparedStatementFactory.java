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

package grevend.persistencelite.internal.service.sql;

import static grevend.persistencelite.crud.Crud.CREATE;

import grevend.persistencelite.crud.Crud;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.entity.EntityProperty;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see PreparedStatement
 * @since 0.2.0
 */
final class PreparedStatementFactory {

    @Nullable
    PreparedStatement prepare(@NotNull Crud crud, @NotNull EntityMetadata<?> entityMetadata, @NotNull SqlTransaction transaction, boolean cached, int limit) {
        var cache = StatementCache.instance().cache();
        if (!cache.containsKey(entityMetadata)) {
            cache.put(entityMetadata, new HashMap<>());
        }

        if (!cache.get(entityMetadata).containsKey(crud)) {
            cache.get(entityMetadata).put(crud, switch (crud) {
                case CREATE -> this.create(entityMetadata);
                case RETRIEVE -> limit == -1 ? this.prepareSelectAll(entityMetadata)
                    : this.retrieve(entityMetadata);
                case UPDATE -> this.update(entityMetadata);
                case DELETE -> this.delete(entityMetadata);
            });
        }

        try {
            var statement = cache.get(entityMetadata).get(crud);
            if (!cached) { cache.get(entityMetadata).remove(crud); }
            return transaction.connection().prepareStatement(statement,
                crud == CREATE ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    @NotNull
    @Contract(pure = true)
    public String escape(@NotNull EntityProperty property) {
        return property.escape() ? this.escape(property.propertyName()) : property.propertyName();
    }

    @NotNull
    @Contract(pure = true)
    public String escape(@NotNull EntityMetadata<?> metadata) {
        return metadata.escape() ? this.escape(metadata.name()) : metadata.name();
    }

    @NotNull
    @Contract(pure = true)
    public String escape(@NotNull String text) {
        return "\"" + text + "\"";
    }

    @NotNull
    private String create(@NotNull EntityMetadata<?> entityMetadata) {
        return "insert into " + this.escape(entityMetadata) + " (" + entityMetadata
            .uniqueProperties().stream().map(this::escape).distinct()
            .collect(Collectors.joining(", ")) + ") values (" + String
            .join(", ", Collections.nCopies(entityMetadata.uniqueProperties().size(), "?")) + ")";
    }

    @NotNull
    @Contract("_, _, _ -> param2")
    public PreparedStatement values(@NotNull Iterable<String> props, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        for (String property : props) {
            var value = properties.get(property);
            if (value == null || value.equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
            i++;
        }
        return statement;
    }

    @NotNull
    private String retrieve(@NotNull EntityMetadata<?> entityMetadata) {
        return this.prepareSelectAll(entityMetadata) + " where " + entityMetadata
            .declaredIdentifiers().stream().map(this::escape)
            .map(prop -> this.escape(entityMetadata) + "." + prop + " = ?")
            .collect(Collectors.joining(" and ")) + " limit 1";
    }

    @NotNull
    private String update(@NotNull EntityMetadata<?> entityMetadata) {
        return "update " + this.escape(entityMetadata) + " set " + entityMetadata.uniqueProperties()
            .stream().map(this::escape).map(prop -> prop + " = ?").collect(Collectors.joining(", "))
            + " where " + entityMetadata.declaredIdentifiers().stream().map(this::escape)
            .map(prop -> prop + " = ?").collect(Collectors.joining(" and "));
    }

    @NotNull
    private String delete(@NotNull EntityMetadata<?> entityMetadata) {
        return "delete from " + this.escape(entityMetadata) + " where " + entityMetadata
            .declaredIdentifiers().stream().map(this::escape)
            .map(prop -> this.escape(entityMetadata) + "." + prop + " = ?")
            .collect(Collectors.joining(" and "));
    }

    /**
     * @param entityMetadata
     * @param attributes
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.2
     */
    @NotNull
    String prepareSelectWithAttributes(@NotNull EntityMetadata<?> entityMetadata, @NotNull Collection<String> attributes) {
        return this.prepareSelectAll(entityMetadata) + " where " + entityMetadata.properties()
            .stream().filter(prop -> attributes.contains(prop.propertyName()) || attributes
                .contains(prop.fieldName())).map(prop ->
                (prop.identifier() != null || prop.copy() ? (this.escape(entityMetadata) + ".")
                    : "") + this.escape(prop) + " = ?").collect(Collectors.joining(" and "));
    }

    /**
     * @param entityMetadata
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private String prepareSelectAll(@NotNull EntityMetadata<?> entityMetadata) {
        var builder = new StringBuilder();
        builder.append("select distinct * from ").append(this.escape(entityMetadata));
        entityMetadata.declaredSuperTypes()
            .forEach(superType -> this.prepareSelectAll(builder, entityMetadata, superType));
        return builder.toString();
    }

    /**
     * @param builder
     * @param parent
     * @param child
     *
     * @see StringBuilder
     * @see EntityMetadata
     * @since 0.2.0
     */
    private void prepareSelectAll(@NotNull StringBuilder builder, @NotNull EntityMetadata<?> parent, @NotNull EntityMetadata<?> child) {
        builder.append(this.prepareInnerJoin(parent, child));
        child.declaredSuperTypes()
            .forEach(superType -> this.prepareSelectAll(builder, child, superType));
    }

    /**
     * @param parent
     * @param child
     *
     * @return
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    private String prepareInnerJoin(@NotNull EntityMetadata<?> parent, @NotNull EntityMetadata<?> child) {
        return " inner join " + this.escape(child) + " on " + child.declaredIdentifiers().stream()
            .map(this::escape)
            .map(prop -> this.escape(parent) + "." + prop + " = " + this.escape(child) + "." + prop)
            .collect(Collectors.joining(" and "));
    }

    /**
     * @author David Greven
     * @see EntityMetadata
     * @see Crud
     * @since 0.3.3
     */
    private static final class StatementCache {

        private static final Object MUTEX = new Object();
        private static volatile StatementCache INSTANCE;
        private final Map<EntityMetadata<?>, Map<Crud, String>> preparedStatementMap;

        /**
         * @since 0.2.0
         */
        @Contract(pure = true)
        private StatementCache() {
            this.preparedStatementMap = new HashMap<>();
        }

        /**
         * @return
         *
         * @since 0.3.3
         */
        @NotNull
        private static StatementCache instance() {
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

        /**
         * @return
         *
         * @since 0.3.3
         */
        @NotNull
        @Contract(pure = true)
        private Map<EntityMetadata<?>, Map<Crud, String>> cache() {
            return this.preparedStatementMap;
        }

    }

}
