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

import grevend.persistencelite.util.sequence.Seq;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A generic implementation of the DAO pattern that provides an abstract interface to some type of
 * data source.
 *
 * @param <E> The type of the entity to which the DAO should apply.
 *
 * @author David Greven
 * @version 0.2.0
 */
public interface Dao<E> {

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
    E create(@NotNull E entity) throws Exception;

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
    Collection<E> create(@NotNull Iterable<E> entities) throws Exception;

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all matching entities
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param properties The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entities found in the form of a {@code Collection}. The returned
     * collection should be immutable to avoid confusion about the synchronization behavior of the
     * contained entities with the data source.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Collection
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    Collection<E> retrieve(@NotNull Map<String, Object> properties) throws Exception;

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all entities the
     * current entity type.
     *
     * @return Returns the entities found in the form of a collection. The returned collection
     * should be immutable to avoid confusion about the synchronization behavior of the contained
     * entities with the data source.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Collection
     * @since 0.2.0
     */
    @NotNull
    Collection<E> retrieve() throws Exception;

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
    E update(@NotNull E entity, @NotNull Map<String, Object> properties) throws Exception;

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
    Collection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> properties) throws Exception;

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entity from the
     * current data source.
     *
     * @param entity The entity that should be deleted.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @since 0.2.0
     */
    void delete(@NotNull E entity) throws Exception;

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
    void delete(@NotNull Iterable<E> entities) throws Exception;

    /**
     * Returns a lazy sequence based on the collection provided by the {@code retrieve()} method.
     *
     * @param <S> The {@code Seq} type used for providing the return types of the chained method
     *            calls.
     *
     * @return Returns a new {@code Seq} based on the provided {@code Iterable}.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Seq
     * @see #retrieve()
     * @see Iterable
     * @since 0.2.0
     */
    @NotNull
    default <S extends Seq<E, S>> Seq<E, S> sequence() throws Exception {
        return Seq.of(this.retrieve());
    }

}

