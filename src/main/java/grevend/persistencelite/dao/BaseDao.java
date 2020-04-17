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

package grevend.persistencelite.dao;

import grevend.persistencelite.entity.EntityFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.util.ExceptionEscapeHatch;
import grevend.persistencelite.util.function.ThrowingConsumer;
import grevend.persistencelite.util.function.ThrowingFunction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * An abstract {@code BaseDao} class that offers standard implementations for some CRUD operations
 * which can be used to simplify creating a custom {@code Dao} implementation. It also enables
 * simplified access to the entity metadata and current transaction.
 *
 * @param <E> The type of the entity to which the {@code Dao} should apply.
 * @param <T> The type of {@code Transaction} that can be used in combination with the
 *            implementation based on this base class.
 *
 * @author David Greven
 * @see Dao
 * @see Transaction
 * @since 0.2.0
 */
public abstract class BaseDao<E, T extends Transaction> implements Dao<E> {

    private final EntityMetadata<E> entityMetadata;
    private final T transaction;

    @Contract(pure = true)
    public BaseDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable T transaction) {
        this.entityMetadata = entityMetadata;
        this.transaction = transaction;
    }

    /**
     * Returns the current {@code Transaction} that will be used by all operations performed on this
     * {@code Dao} instance.
     *
     * @return Returns either the current transaction or null if unavailable.
     *
     * @see Dao
     * @see Transaction
     * @since 0.2.0
     */
    @Nullable
    protected T getTransaction() {
        return this.transaction;
    }

    /**
     * Returns the metadata of the entity that this {@code Dao} applies to.
     *
     * @return The metadata of the entity.
     *
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    protected EntityMetadata<E> getEntityMetadata() {
        return this.entityMetadata;
    }

    /**
     * An implementation of the <b>create</b> CRUD operation that persists an entity.
     *
     * @param entity The entity to be persisted.
     *
     * @return Either returns the entity from the first parameter or creates a new instance based on
     * the persistent version.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @since 0.2.0
     */
    @NotNull
    @Override
    public E create(@NotNull E entity) throws Exception {
        return this.create(entity, EntityFactory.deconstruct(this.entityMetadata, entity));
    }

    /**
     * An implementation of the <b>create</b> CRUD operation that persists none, one or many
     * entities.
     *
     * @param entities An {@code Iterable} that provides the entities that should be persisted.
     *
     * @return Either returns the iterated entities from the first parameter or creates a new
     * collection based on the persistent versions. The returned collection should be immutable to
     * avoid confusion about the synchronization behavior of the contained entities with the data
     * source.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Collection
     * @see Iterable
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Collection<E> create(@NotNull Iterable<E> entities) throws Exception {
        final var escapeHatch = new ExceptionEscapeHatch();
        var res = StreamSupport.stream(entities.spliterator(), false).map(ExceptionEscapeHatch
            .escape((@NotNull ThrowingFunction<E, E>) this::create, escapeHatch))
            .filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
        escapeHatch.rethrow();
        return res;
    }

    @NotNull
    protected abstract E create(@NotNull E entity, @NotNull Collection<Map<String, Object>> properties) throws Exception;

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
     * @throws Exception If an error occurs during the persistence process.
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public E update(@NotNull E entity, @NotNull Map<String, Object> properties) throws Exception {
        return null;
    }

    /**
     * An implementation of the <b>update</b> CRUD operation which returns an updated versions of
     * the provided entities. An {@code Iterable} of properties that should be updated are passed in
     * as the second parameter in the form of a {@code Map}.
     *
     * @param entities   The immutable entities that should be updated.
     * @param properties The {@code Iterable} of key-value pair {@code Map} objects that represents
     *                   the properties and their updated values.
     *
     * @return Returns the updated entity.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Collection
     * @see Iterable
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Unmodifiable
    public Collection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> properties) throws Exception {
        return List.of();
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entity from the
     * current data source.
     *
     * @param entity The entity that should be deleted.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @since 0.2.0
     */
    @Override
    public void delete(@NotNull E entity) throws Exception {
        var deconstructed = EntityFactory.deconstruct(this.entityMetadata, entity);
        if (!deconstructed.isEmpty()) {
            this.delete(deconstructed.iterator().next());
        }
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entities from
     * the current data source.
     *
     * @param entities The {@code Iterable} of entities that should be deleted.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Iterable
     * @since 0.2.0
     */
    @Override
    public void delete(@NotNull Iterable<E> entities) throws Exception {
        final var escapeHatch = new ExceptionEscapeHatch();
        entities.forEach(
            ExceptionEscapeHatch.escape((@NotNull ThrowingConsumer<E>) this::delete, escapeHatch));
        /*StreamSupport.stream(entities.spliterator(), false)
            .map(ExceptionEscapeHatch.escape(this::delete, escapeHatch)).filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());*/
        escapeHatch.rethrow();
    }

}
