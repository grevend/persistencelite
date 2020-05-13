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

package grevend.persistencelite.internal.dao;

import grevend.common.Failure;
import grevend.common.FailureCollection;
import grevend.common.Result;
import grevend.common.ResultCollection;
import grevend.persistencelite.dao.Dao;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FailureDao<E> implements Dao<E>, Failure<Dao<E>> {

    private final Failure<E> failure;

    @Contract(pure = true)
    public FailureDao(@NotNull Failure<?> failure) {
        this.failure = Failure.of(failure);
    }

    @NotNull
    @Override
    public Throwable reason() {
        return this.failure.reason();
    }

    /**
     * {@inheritDoc}
     *
     * @param entity The entity to be persisted.
     *
     * @return Either returns the entity from the first parameter or creates a new instance based on
     * the persistent version.
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Result<E> create(@NotNull E entity) {
        return this.failure;
    }

    /**
     * {@inheritDoc}
     *
     * @param entities An {@code Iterable} that provides the entities that should be persisted.
     *
     * @return Either returns the iterated entities from the first parameter or creates a new
     * collection based on the persistent versions. The returned collection should be immutable to
     * avoid confusion about the synchronization behavior of the contained entities with the data
     * source.
     *
     * @see Collection
     * @see Iterable
     * @since 0.2.0
     */
    @NotNull
    @Override
    public ResultCollection<E> create(@NotNull Iterable<E> entities) {
        return FailureCollection.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @param identifiers The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entity found in the form of an {@code Optional}.
     *
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Result<E> retrieveById(@NotNull Map<String, Object> identifiers) {
        return this.failure;
    }

    /**
     * {@inheritDoc}
     *
     * @param properties The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entities found in the form of an {@code Collection}.
     *
     * @see Collection
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public ResultCollection<E> retrieveByProps(@NotNull Map<String, Object> properties) {
        return FailureCollection.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @return Returns the entities found in the form of a collection. The returned collection
     * should be immutable to avoid confusion about the synchronization behavior of the contained
     * entities with the data source.
     *
     * @see Collection
     * @since 0.2.0
     */
    @NotNull
    @Override
    public ResultCollection<E> retrieveAll() {
        return FailureCollection.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @param entity     The immutable entity that should be updated.
     * @param properties The {@code Map} of key-value pairs that represents the properties and their
     *                   updated values.
     *
     * @return Returns the updated entity.
     *
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Result<E> update(@NotNull E entity, @NotNull Map<String, Object> properties) {
        return this.failure;
    }

    /**
     * {@inheritDoc}
     *
     * @param entities   The immutable entities that should be updated.
     * @param properties The {@code Iterable} of key-value pair {@code Map} objects that represents
     *                   the properties and their updated values.
     *
     * @return Returns the updated entity.
     *
     * @see Collection
     * @see Iterable
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    public ResultCollection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> properties) {
        return FailureCollection.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @param entity The entity that should be deleted.
     *
     * @since 0.2.0
     */
    @Override
    public @NotNull Result<Void> delete(@NotNull E entity) {
        return Result.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @param identifiers The identifiers that should be used to delete the entity.
     *
     * @since 0.2.0
     */
    @Override
    public @NotNull Result<Void> delete(@NotNull Map<String, Object> identifiers) {
        return Result.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @param entities The {@code Iterable} of entities that should be deleted.
     *
     * @see Iterable
     * @since 0.2.0
     */
    @Override
    public @NotNull Result<Void> delete(@NotNull Iterable<E> entities) {
        return Result.of(this.failure);
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception if this resource cannot be closed
     * @since 0.3.3
     */
    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException();
    }

}
