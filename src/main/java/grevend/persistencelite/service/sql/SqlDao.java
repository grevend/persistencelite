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

import grevend.persistencelite.dao.BaseDao;
import grevend.persistencelite.entity.EntityFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.entity.EntityProperty;
import grevend.persistencelite.service.sql.PreparedStatementFactory.StatementType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class SqlDao<E> extends BaseDao<E, SqlTransaction> {

    private final PreparedStatementFactory preparedStatementFactory;

    SqlDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable SqlTransaction transaction) {
        super(entityMetadata, transaction);
        this.preparedStatementFactory = new PreparedStatementFactory();
    }

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

    private void create(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) throws SQLException {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.INSERT,
            Objects.requireNonNull(this.getTransaction()).connection(), entityMetadata);
        this.setCreateStatementValues(entityMetadata, preparedStatement, properties);
        preparedStatement.executeUpdate();
    }

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
        return res.next() ? Optional.of(EntityFactory.construct(this.getEntityMetadata(), res))
            : Optional.empty();
    }

    private void setRetrieveStatementValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        for (EntityProperty property : entityMetadata.getIdentifiers()) {
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
        Collection<E> entities = new ArrayList<E>();
        while (res.next()) {
            entities.add(EntityFactory.construct(this.getEntityMetadata(), res));
        }
        return Collections.unmodifiableCollection(entities);
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

    }

}
