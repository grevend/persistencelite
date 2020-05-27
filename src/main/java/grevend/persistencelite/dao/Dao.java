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

import grevend.common.Result;
import grevend.common.ResultCollection;
import grevend.common.SuccessCollection;
import grevend.sequence.Seq;
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
public interface Dao<E> extends AutoCloseable {

    /**
     * An implementation of the <b>create</b> CRUD operation that persists an entity.
     *
     * @param entity The entity to be persisted.
     *
     * @return Either returns the entity from the first parameter or creates a new instance based on
     * the persistent version.
     *
     * @since 0.2.0
     */
    @NotNull
    Result<E> create(@NotNull E entity);

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
     * @see ResultCollection
     * @see Iterable
     * @since 0.2.0
     */
    @NotNull
    ResultCollection<E> create(@NotNull Iterable<E> entities);

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns the matching entity
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param identifiers The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entity found in the form of an {@code Result}.
     *
     * @see Result
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    Result<E> retrieveById(@NotNull Map<String, Object> identifiers);

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns the matching entity
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param key   The key component.
     * @param value The value component.
     *
     * @return Returns the entity found in the form of an {@code Result}.
     *
     * @see Result
     * @since 0.2.0
     */
    @NotNull
    default Result<E> retrieveById(@NotNull String key, @NotNull Object value) {
        return this.retrieveById(Map.of(key, value));
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all matching entities
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param properties The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entities found in the form of an {@code Collection}.
     *
     * @see ResultCollection
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    ResultCollection<E> retrieveByProps(@NotNull Map<String, Object> properties);

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all matching entities
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param key   The key component.
     * @param value The value component.
     *
     * @return Returns the entities found in the form of an {@code Collection}.
     *
     * @see ResultCollection
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    default ResultCollection<E> retrieveByProps(@NotNull String key, @NotNull Object value) {
        return this.retrieveByProps(Map.of(key, value));
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns the first matching
     * entity based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param properties The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the first entity found in the form of an {@code Result}.
     *
     * @see Result
     * @see Map
     * @since 0.4.8
     */
    @NotNull
    Result<E> retrieveFirstByProps(@NotNull Map<String, Object> properties);

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns the first matching
     * entity based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param key   The key component.
     * @param value The value component.
     *
     * @return Returns the first entity found in the form of an {@code Result}.
     *
     * @see Result
     * @since 0.4.8
     */
    @NotNull
    default Result<E> retrieveFirstByProps(@NotNull String key, @NotNull Object value) {
        return this.retrieveFirstByProps(Map.of(key, value));
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all entities the
     * current entity type.
     *
     * @return Returns the entities found in the form of a collection. The returned collection
     * should be immutable to avoid confusion about the synchronization behavior of the contained
     * entities with the data source.
     *
     * @see ResultCollection
     * @since 0.2.0
     */
    @NotNull
    ResultCollection<E> retrieveAll();

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
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    Result<E> update(@NotNull E entity, @NotNull Map<String, Object> properties);

    /**
     * An implementation of the <b>update</b> CRUD operation which returns an updated version of the
     * provided entity. The properties that should be updated are passed in as the second parameter
     * in the form of a {@code Map}.
     *
     * @param entity The immutable entity that should be updated.
     * @param key    The key component.
     * @param value  The value component.
     *
     * @return Returns the updated entity.
     *
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    default Result<E> update(@NotNull E entity, @NotNull String key, @NotNull Object value) {
        return this.update(entity, Map.of(key, value));
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
     * @see ResultCollection
     * @see Iterable
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    ResultCollection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> properties);

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entity from the
     * current data source.
     *
     * @param entity The entity that should be deleted.
     *
     * @since 0.2.0
     */
    @NotNull
    Result<Void> delete(@NotNull E entity);

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes an entity based on the
     * identifiers from the current data source.
     *
     * @param identifiers The identifiers that should be used to delete the entity.
     *
     * @since 0.2.0
     */
    @NotNull
    Result<Void> delete(@NotNull Map<String, Object> identifiers);

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes an entity based on the
     * identifiers from the current data source.
     *
     * @param key   The key component.
     * @param value The value component.
     *
     * @since 0.2.0
     */
    @NotNull
    default Result<Void> delete(@NotNull String key, @NotNull Object value) {
        return this.delete(Map.of(key, value));
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entities from
     * the current data source.
     *
     * @param entities The {@code Iterable} of entities that should be deleted.
     *
     * @see Iterable
     * @since 0.2.0
     */
    @NotNull
    Result<Void> delete(@NotNull Iterable<E> entities);

    /**
     * Returns a lazy grevend.sequence based on the collection provided by the {@code retrieve()}
     * method.
     *
     * @param <S> The {@code Seq} type used for providing the return types of the chained method
     *            calls.
     *
     * @return Returns a new {@code Seq} based on the provided {@code Iterable}.
     *
     * @see Seq
     * @see #retrieveAll()
     * @see Iterable
     * @since 0.2.0
     */
    @NotNull
    default <S extends Seq<E, S>> Seq<E, S> sequence() {
        return this.retrieveAll() instanceof SuccessCollection<E> collection ? Seq
            .of(collection.get()) : Seq.empty();
    }

}

