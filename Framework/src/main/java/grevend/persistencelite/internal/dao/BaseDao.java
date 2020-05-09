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

import grevend.common.Pair;
import grevend.persistencelite.dao.Dao;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.entity.factory.EntityFactory;
import grevend.persistencelite.internal.entity.representation.EntityDeserializer;
import grevend.persistencelite.internal.entity.representation.EntitySerializer;
import grevend.persistencelite.internal.util.Utils;
import grevend.sequence.function.ThrowableEscapeHatch;
import grevend.sequence.function.ThrowingFunction;
import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <E>
 *
 * @author David Greven
 * @since 0.3.3
 */
public class BaseDao<E, Thr extends Exception> implements Dao<E> {

    private final DaoImpl<Thr> daoImpl;
    private final EntitySerializer<E> entitySerializer;
    private final EntityDeserializer<E> entityDeserializer;
    private Transaction transaction = null;

    @Contract(pure = true)
    public BaseDao(@NotNull EntityMetadata<E> entityMetadata, @NotNull DaoImpl<Thr> daoImpl, @NotNull TransactionFactory transactionFactory, @Nullable Transaction transaction, boolean props) throws Throwable {
        this.daoImpl = daoImpl;
        this.transaction = transaction == null ?
            transactionFactory.createTransaction() : transaction;
        this.entitySerializer = entity -> EntityFactory.deconstruct(entityMetadata, entity);
        this.entityDeserializer = map -> EntityFactory.construct(entityMetadata, map, props);
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
     * @since 0.3.3
     */
    @NotNull
    @Override
    public E create(@NotNull E entity) throws Throwable {
        var entityComponents = this.entitySerializer.serialize(entity);
        this.daoImpl.create(entityComponents);
        var iter = this.daoImpl.retrieve(this.entitySerializer.merge(entityComponents)).iterator();
        if (!iter.hasNext()) { throw new IllegalStateException(""); }
        return this.entityDeserializer.deserialize(iter.next());
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
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Collection<E> create(@NotNull Iterable<E> entities) throws Throwable {
        final var escapeHatch = new ThrowableEscapeHatch<>(Throwable.class);
        var res = StreamSupport.stream(entities.spliterator(), false)
            .map(ThrowableEscapeHatch
                .escape((@NotNull ThrowingFunction<E, E>) this::create, escapeHatch))
            .filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
        escapeHatch.rethrow();
        return res;
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns the matching entity
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param identifiers The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entity found in the form of an {@code Optional}.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Optional
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Optional<E> retrieveById(@NotNull Map<String, Object> identifiers) throws Throwable {
        var iter = this.retrieveByProps(identifiers).iterator();
        return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all matching entities
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param props The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entities found in the form of an {@code Collection}.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Collection
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Collection<E> retrieveByProps(@NotNull Map<String, Object> props) throws Throwable {
        final var escapeHatch = new ThrowableEscapeHatch<>(Throwable.class);
        var res = StreamSupport.stream(this.daoImpl.retrieve(props).spliterator(), false)
            .map(ThrowableEscapeHatch.escape(this.entityDeserializer::deserialize, escapeHatch))
            .collect(Collectors.toUnmodifiableList());
        escapeHatch.rethrow();
        return res;
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
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Collection<E> retrieveAll() throws Throwable {
        return this.retrieveByProps(Map.of());
    }

    /**
     * An implementation of the <b>update</b> CRUD operation which returns an updated version of the
     * provided entity. The properties that should be updated are passed in as the second parameter
     * in the form of a {@code Map}.
     *
     * @param entity The immutable entity that should be updated.
     * @param props  The {@code Map} of key-value pairs that represents the properties and their
     *               updated values.
     *
     * @return Returns the updated entity.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public E update(@NotNull E entity, @NotNull Map<String, Object> props) throws Throwable {
        var components = this.entitySerializer.serialize(entity);
        this.daoImpl.update(components, props);
        var iter = this.daoImpl.retrieve(Stream.of(this.entitySerializer.merge(components), props)
            .flatMap(map -> map.entrySet().stream()).collect(Collectors
                .toUnmodifiableMap(Entry::getKey, Entry::getValue,
                    (oldEntry, newEntry) -> newEntry))).iterator();
        if (!iter.hasNext()) { throw new IllegalStateException(""); }
        return this.entityDeserializer.deserialize(iter.next());
    }

    /**
     * An implementation of the <b>update</b> CRUD operation which returns an updated versions of
     * the provided entities. An {@code Iterable} of properties that should be updated are passed in
     * as the second parameter in the form of a {@code Map}.
     *
     * @param entities The immutable entities that should be updated.
     * @param props    The {@code Iterable} of key-value pair {@code Map} objects that represents
     *                 the properties and their updated values.
     *
     * @return Returns the updated entity.
     *
     * @throws Throwable If an error occurs during the persistence process.
     * @see Collection
     * @see Iterable
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Collection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> props) throws Throwable {
        final var escapeHatch = new ThrowableEscapeHatch<>(Throwable.class);
        var res = Utils.zip(entities.iterator(), props.iterator()).map(ThrowableEscapeHatch
            .escape((Pair<E, Map<String, Object>> pair) -> this
                .update(Objects.requireNonNull(pair).first(), pair.second()), escapeHatch))
            .collect(Collectors.toUnmodifiableList());
        escapeHatch.rethrow();
        return res;
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entity from the
     * current data source.
     *
     * @param entity The entity that should be deleted.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @since 0.3.3
     */
    @Override
    public void delete(@NotNull E entity) throws Throwable {
        this.delete(this.entitySerializer.merge(this.entitySerializer.serialize(entity)));
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes an entity based on the
     * identifiers from the current data source.
     *
     * @param identifiers The identifiers that should be used to delete the entity.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @since 0.3.3
     */
    @Override
    public void delete(@NotNull Map<String, Object> identifiers) throws Throwable {
        this.daoImpl.delete(identifiers);
    }

    /**
     * An implementation of the <b>delete</b> CRUD operation which deletes the given entities from
     * the current data source.
     *
     * @param entities The {@code Iterable} of entities that should be deleted.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Iterable
     * @since 0.3.3
     */
    @Override
    public void delete(@NotNull Iterable<E> entities) throws Throwable {
        final var escapeHatch = new ThrowableEscapeHatch<>(Throwable.class);
        entities.forEach(ThrowableEscapeHatch
            .escape((E entity) -> this.delete(Objects.requireNonNull(entity)), escapeHatch));
        escapeHatch.rethrow();
    }

    /**
     * Closes this resource, relinquishing any underlying resources. This method is invoked
     * automatically on objects managed by the {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to declare concrete implementations
     * of the {@code close} method to throw more specific exceptions, or to throw no exception at
     * all if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish the underlying resources and
     * to internally <em>mark</em> the resource as closed, prior to throwing the exception. The
     * {@code close} method is unlikely to be invoked more than once and so this ensures that the
     * resources are released in a timely manner. Furthermore it reduces problems that could arise
     * when the resource wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status, and runtime misbehavior is
     * likely to occur if an {@code InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an exception to be suppressed, the {@code
     * AutoCloseable.close} method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method is <em>not</em> required to be
     * idempotent.  In other words, calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is required to have no effect if
     * called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged to make their {@code close}
     * methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     * @since 0.3.3
     */
    @Override
    public void close() throws Exception {
        if (this.transaction != null) {
            this.transaction.close();
        }
    }

}
