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

import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.BaseDao;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.entity.factory.EntityFactory;
import grevend.persistencelite.internal.service.sql.PreparedStatementFactory.StatementType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * @param <E> The type of the entity to which this DAO implementation should apply.
 *
 * @author David Greven
 * @version 0.2.0
 * @see BaseDao
 * @see SqlTransaction
 */
public final class SqlDao<E> extends BaseDao<E, SqlTransaction> {

    private final Supplier<Transaction> transactionSupplier;
    private final PreparedStatementFactory preparedStatementFactory;

    /**
     * @param entityMetadata
     * @param transaction
     *
     * @since 0.2.0
     */
    SqlDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable SqlTransaction transaction, @NotNull Supplier<Transaction> transactionSupplier) {
        super(entityMetadata, transaction);
        this.transactionSupplier = transactionSupplier;
        this.preparedStatementFactory = new PreparedStatementFactory();
    }

    /**
     * @param entity
     * @param properties
     *
     * @return
     *
     * @throws SQLException
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract("_, _ -> param1")
    protected E create(@NotNull E entity, @NotNull Collection<Map<String, Object>> properties) throws SQLException {
        var iterator = this.getEntityMetadata().getSuperTypes().iterator();
        for (Map<String, Object> props : properties) {
            this.create(iterator.hasNext() ? iterator.next() : this.getEntityMetadata(), props);
        }
        return entity;
    }

    /**
     * @param entityMetadata
     * @param properties
     *
     * @throws SQLException
     * @since 0.2.0
     */
    private void create(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) throws SQLException {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.INSERT,
            Objects.requireNonNull(this.getTransaction()).connection(), entityMetadata);
        this.setCreateStatementValues(entityMetadata, preparedStatement, properties);
        preparedStatement.executeUpdate();
    }

    /**
     * @param entityMetadata
     * @param statement
     * @param properties
     *
     * @throws SQLException
     * @since 0.2.0
     */
    private void setCreateStatementValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        for (EntityProperty property : entityMetadata.getUniqueProperties()) {
            var value = properties.get(property.propertyName());
            if (value == null || value.equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
            i++;
        }
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all matching entities
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param identifiers The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entity found in the form of an {@code Optional}.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Optional
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Optional<E> retrieve(@NotNull Map<String, Object> identifiers) throws Throwable {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.SELECT,
            Objects.requireNonNull(this.getTransaction()).connection(), this.getEntityMetadata());
        this.setRetrieveStatementValues(this.getEntityMetadata(), preparedStatement, identifiers);
        var res = preparedStatement.executeQuery();
        return res.next() ? Optional.of(EntityFactory.construct(this.getEntityMetadata(), res, Map.of()))
            : Optional.empty();
    }

    /**
     * @param entityMetadata
     * @param statement
     * @param properties
     *
     * @throws SQLException
     * @since 0.2.0
     */
    private void setRetrieveStatementValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        for (EntityProperty property : entityMetadata.getDeclaredIdentifiers()) {
            var value = properties.get(property.propertyName());
            if (value == null || value.equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
            i++;
        }
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all entities the
     * current entity type.
     *
     * @return Returns the entities found in the form of a collection. The returned collection
     * should be immutable to avoid confusion about the synchronization behavior of the contained
     * entities with the data source.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Collection
     * @since 0.2.0
     */
    @NotNull
    @Override
    @UnmodifiableView
    public Collection<E> retrieve() throws Throwable {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.SELECT_ALL,
            Objects.requireNonNull(this.getTransaction()).connection(), this.getEntityMetadata());
        var res = preparedStatement.executeQuery();
        Collection<E> entities = new ArrayList<>();
        while (res.next()) {
            entities.add(EntityFactory.construct(this.getEntityMetadata(), res, Map.of()));
        }
        return Collections.unmodifiableCollection(entities);
    }

    /**
     * An implementation of the <b>update</b> CRUD operation which returns an updated version of the
     * provided entity. The properties that should be updated are passed in as the second parameter
     * in the form of a {@code Map}.
     *
     * @param entity     The immutable entity that should be updated.
     * @param properties The {@code Map} of key-value pairs that represents the properties and their
     *                   updated values.
     *
     * @return Returns the updated entity.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public E update(@NotNull E entity, @NotNull Map<String, Object> properties) throws Throwable {
        var iterator = this.getEntityMetadata().getSuperTypes().iterator();
        var deconstructed = EntityFactory.deconstruct(this.getEntityMetadata(), entity);

        var props = Stream.concat(
            Stream.of(deconstructed)
                .flatMap(Collection::stream).flatMap(map -> map.entrySet().stream()).collect(
                Collectors.toMap(Entry::getKey, Entry::getValue, (entry1, entry2) -> entry2))
                .entrySet().stream(), properties.entrySet().stream()).collect(Collectors
            .toUnmodifiableMap(Entry::getKey, Entry::getValue, (entry1, entry2) -> entry2));

        for (Map<String, Object> stringObjectMap : deconstructed) {
            if (stringObjectMap.keySet().stream().anyMatch(properties::containsKey)) {
                this.update(iterator.next(), props);
            } else {
                if (iterator.hasNext()) {
                    iterator.next();
                }
            }
        }

        return Objects.requireNonNull(this.retrieve(props).orElse(null));
    }

    private void update(@NotNull EntityMetadata<?> child, @NotNull Map<String, Object> properties) throws Throwable {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.UPDATE,
            Objects.requireNonNull(this.getTransaction()).connection(), child);
        this.setUpdateStatementValues(child, preparedStatement, properties);
        preparedStatement.executeUpdate();
    }

    /**
     * @param entityMetadata
     * @param statement
     * @param properties
     *
     * @throws SQLException
     * @since 0.2.0
     */
    private void setUpdateStatementValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        for (EntityProperty property : entityMetadata.getUniqueProperties()) {
            var value = properties.get(property.propertyName());
            if (value == null || value.equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
            i++;
        }
        for (EntityProperty property : entityMetadata.getDeclaredIdentifiers()) {
            var value = properties.get(property.propertyName());
            if (value == null || value.equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
            i++;
        }
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes an entity based on the
     * identifiers from the current data source.
     *
     * @param identifiers The identifiers that should be used to delete the entity.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @since 0.2.0
     */
    @Override
    public void delete(@NotNull Map<String, Object> identifiers) throws Exception {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.DELETE,
            Objects.requireNonNull(this.getTransaction()).connection(), this.getEntityMetadata());
        this.setDeleteStatementValues(this.getEntityMetadata(), preparedStatement, identifiers);
        preparedStatement.executeUpdate();
    }

    /**
     * @param entityMetadata
     * @param statement
     * @param properties
     *
     * @throws SQLException
     * @since 0.2.0
     */
    private void setDeleteStatementValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        for (EntityProperty property : entityMetadata.getDeclaredIdentifiers()) {
            var value = properties.get(property.propertyName());
            if (value == null || value.equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, value);
            }
            i++;
        }
    }

}
