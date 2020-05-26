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

import grevend.persistencelite.crud.Crud;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.util.Utils;
import grevend.sequence.Seq;
import grevend.sequence.function.ThrowableEscapeHatch;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * @param <E>
 *
 * @author David Greven
 * @since 0.3.3
 */
public final record SqlDao<E>(@NotNull EntityMetadata<E>entityMetadata, @NotNull SqlTransaction transaction, @NotNull TransactionFactory transactionFactory, @NotNull PreparedStatementFactory preparedStatementFactory) implements DaoImpl<SQLException> {

    @Contract(pure = true)
    public SqlDao(@NotNull EntityMetadata<E> entityMetadata, @NotNull SqlTransaction transaction, @NotNull TransactionFactory transactionFactory) {
        this(entityMetadata, transaction, transactionFactory, new PreparedStatementFactory());
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws SQLException {
        final var escapeHatch = new ThrowableEscapeHatch<>(SQLException.class);

        Utils.zip(this.entityMetadata.superTypes().iterator(), entity.iterator())
            .filter(Objects::nonNull).forEach(ThrowableEscapeHatch.escapeSuper(
            pair -> {
                var statement = this.preparedStatementFactory.values(
                    Objects.requireNonNull(pair).first().uniqueProperties().stream()
                        .map(EntityProperty::propertyName).collect(Collectors.toUnmodifiableList()),
                    Objects.requireNonNull(this.preparedStatementFactory
                        .prepare(Crud.CREATE, pair.first(), this.transaction, true, -1)), pair.second());
                statement.executeUpdate();
                System.out.println(statement.getGeneratedKeys());
            }, escapeHatch));

        escapeHatch.rethrow();
    }

    @NotNull
    @Override
    @UnmodifiableView
    public Iterable<Map<String, Object>> retrieve(@NotNull Iterable<String> keys, @NotNull Map<String, Object> props) throws SQLException {
        var preparedStatement = this.preparedStatementFactory.values(keys, Objects.requireNonNull(
            (Utils.containsExactly(keys,
                Seq.of(this.entityMetadata.declaredProperties()).map(EntityProperty::propertyName)
                    .toList()) || props.isEmpty()) ? this.preparedStatementFactory
                .prepare(Crud.RETRIEVE, this.entityMetadata, this.transaction,
                    props.entrySet().isEmpty(), props.entrySet().isEmpty() ? -1 : 1)
                : this.transaction.connection().prepareStatement(this.preparedStatementFactory
                    .prepareSelectWithAttributes(this.entityMetadata, Seq.of(keys).toList()))),
            props);

        var res = SqlUtils.convert(preparedStatement.executeQuery());
        for (var map : res) {
            SqlUtils.createRelationValues(this.entityMetadata, map, () -> {
                try {
                    return this.transactionFactory.createTransaction();
                } catch (Throwable throwable) {
                    return null;
                }
            });
        }

        return Collections.unmodifiableCollection(res);
    }

    @Override
    public void update(@NotNull Iterable<Map<String, Object>> entity, @NotNull Map<String, Object> props) throws SQLException {
        var superTypes = this.entityMetadata.superTypes().iterator();

        var mergedProps = Stream.concat(StreamSupport.stream(entity.spliterator(), false)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (entry1, entry2) -> entry2))
            .entrySet().stream(), props.entrySet().stream()).collect(Collectors
            .toUnmodifiableMap(Entry::getKey, Entry::getValue, (entry1, entry2) -> entry2));

        for (var component : entity) {
            if (component.keySet().stream().anyMatch(props::containsKey)) {
                var superType = superTypes.next();
                this.preparedStatementFactory.values(Stream
                    .concat(superType.uniqueProperties().stream(),
                        superType.declaredIdentifiers().stream()).map(EntityProperty::propertyName)
                    .collect(Collectors.toUnmodifiableList()), Objects.requireNonNull(
                    this.preparedStatementFactory
                        .prepare(Crud.UPDATE, superType, this.transaction, true, -1)), mergedProps)
                    .executeUpdate();
            } else {
                if (superTypes.hasNext()) {
                    superTypes.next();
                }
            }
        }
    }

    @Override
    public void delete(@NotNull Map<String, Object> props) throws SQLException {
        this.preparedStatementFactory.values(
            this.entityMetadata.declaredIdentifiers().stream().map(EntityProperty::propertyName)
                .collect(Collectors.toUnmodifiableList()), Objects.requireNonNull(
                this.preparedStatementFactory
                    .prepare(Crud.DELETE, this.entityMetadata, this.transaction, true, -1)), props)
            .executeUpdate();
    }

}
