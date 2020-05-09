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

import static grevend.sequence.function.ThrowableEscapeHatch.escape;

import grevend.common.Lazy;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.entity.EntityRelation;
import grevend.persistencelite.internal.entity.EntityType;
import grevend.persistencelite.internal.entity.factory.EntityFactory;
import grevend.sequence.function.ThrowableEscapeHatch;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @since 0.2.4
 */
final class SqlUtils {

    /**
     * @param preparedStatement
     *
     * @throws SQLException
     * @since 0.2.4
     */
    private static void setRetrieveStatementValues(@NotNull PreparedStatement preparedStatement, @NotNull EntityMetadata<?> entityMetadata, @NotNull EntityRelation entityRelation, @NotNull Map<String, Object> values) throws SQLException {
        var i = 1;
        var selfProperties = List.of(entityRelation.getSelfProperties());
        for (EntityProperty property : entityMetadata.properties().stream().filter(
            prop -> selfProperties.contains(prop.propertyName()) || selfProperties
                .contains(prop.fieldName())).collect(Collectors.toUnmodifiableList())) {
            var value = values.get(property.propertyName());
            if (value == null || value.equals("null")) {
                preparedStatement.setNull(i, Types.NULL);
            } else {
                preparedStatement.setObject(i, value);
            }
            i++;
        }
    }

    /**
     * @param entityMetadata
     * @param map
     *
     * @since 0.2.4
     */
    static void createRelationValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> map, @NotNull Supplier<Transaction> transactionSupplier) {
        entityMetadata.declaredRelations().forEach(relation -> map.put(relation.fieldName(),
            relation.type().isAssignableFrom(Collection.class) ? new SqlRelation<>(entityMetadata,
                Objects.requireNonNull(relation.relation()), map, transactionSupplier)
                : (relation.type().isAssignableFrom(Lazy.class) ? new Lazy<>(() -> SqlUtils
                    .retrieve(entityMetadata, Objects.requireNonNull(relation.relation()), map,
                        transactionSupplier).stream().findFirst().orElse(null)) : null)));
    }

    /**
     * @param entityMetadata
     * @param entityRelation
     * @param values
     * @param transactionSupplier
     * @param <E>
     *
     * @return
     *
     * @since 0.2.4
     */
    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    static <E> List<E> retrieve(@NotNull EntityMetadata<?> entityMetadata, @NotNull EntityRelation entityRelation, @NotNull Map<String, Object> values, @NotNull Supplier<Transaction> transactionSupplier) {
        List<E> elements = new ArrayList<>();
        PreparedStatementFactory preparedStatementFactory = new PreparedStatementFactory();
        var transaction = transactionSupplier.get();
        if (!(transaction instanceof SqlTransaction)) {
            throw new IllegalStateException();
        } else {
            try {
                var targetMetadata = EntityMetadata.of(entityRelation.getTargetEntity());
                Collection<EntityMetadata<?>> types;
                if (targetMetadata.entityType() == EntityType.INTERFACE) {
                    types = targetMetadata.subTypes();
                } else {
                    types = List.of(targetMetadata);
                }

                for (var subType : types) {
                    var preparedStatement = ((SqlTransaction) transaction).connection()
                        .prepareStatement(preparedStatementFactory
                            .prepareSelectWithAttributes(subType,
                                List.of(entityRelation.getTargetProperties())));
                    setRetrieveStatementValues(preparedStatement, entityMetadata, entityRelation,
                        values);

                    var res = convert(preparedStatement.executeQuery());
                    for (var map : res) {
                        createRelationValues(subType, map, transactionSupplier);
                    }

                    var exceptionEscapeHatch = new ThrowableEscapeHatch<>(Throwable.class);
                    var entities = res.stream().map(escape(
                        (Map<String, Object> map) -> EntityFactory
                            .construct(subType, map, true), exceptionEscapeHatch))
                        .filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
                    exceptionEscapeHatch.rethrow();
                    elements.addAll((List<E>) entities);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return elements;
    }

    /**
     * @param resultSet
     *
     * @return
     *
     * @throws SQLException
     * @since 0.2.4
     */
    @NotNull
    static Collection<Map<String, Object>> convert(@NotNull ResultSet resultSet) throws SQLException {
        Collection<Map<String, Object>> res = new ArrayList<>();
        var metadata = resultSet.getMetaData();
        var columns = metadata.getColumnCount();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (var i = 1; i <= columns; i++) {
                row.put(metadata.getColumnName(i), resultSet.getObject(i));
            }
            res.add(row);
        }
        return res;
    }

}
