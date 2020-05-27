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
import grevend.common.Pair;
import grevend.common.Result;
import grevend.common.ResultCollection;
import grevend.common.SuccessCollection;
import grevend.persistencelite.dao.Dao;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.entity.EntityProperty;
import grevend.persistencelite.internal.entity.factory.EntityFactory;
import grevend.persistencelite.internal.entity.representation.EntityDeserializer;
import grevend.persistencelite.internal.entity.representation.EntitySerializer;
import grevend.persistencelite.internal.util.Utils;
import grevend.sequence.Seq;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private final EntityMetadata<E> entityMetadata;
    private final DaoImpl<Thr> daoImpl;
    private final EntitySerializer<E> entitySerializer;
    private final EntityDeserializer<E> entityDeserializer;
    private Transaction transaction = null;

    @Contract(pure = true)
    public BaseDao(@NotNull EntityMetadata<E> entityMetadata, @NotNull DaoImpl<Thr> daoImpl, @NotNull TransactionFactory transactionFactory, @Nullable Transaction transaction, boolean props) throws Throwable {
        this.entityMetadata = entityMetadata;
        this.daoImpl = daoImpl;
        this.transaction = transaction == null ?
            transactionFactory.createTransaction() : transaction;
        this.entitySerializer = entity -> EntityFactory.deconstruct(entityMetadata, entity);
        this.entityDeserializer = map -> EntityFactory.construct(entityMetadata, map, props);
    }

    /**
     * {@inheritDoc}
     *
     * @param entity The entity to be persisted.
     *
     * @return Either returns the entity from the first parameter or creates a new instance based on
     * the persistent version.
     *
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Result<E> create(@NotNull E entity) {
        return Result.ofThrowing(() -> {
            var entityComponents = this.entitySerializer.serialize(entity);
            this.daoImpl.create(entityComponents);
            var merged = this.entitySerializer.merge(entityComponents);
            var iter = this.daoImpl.retrieve(
                Seq.of(this.entityMetadata.declaredIdentifiers()).map(EntityProperty::propertyName)
                    .toUnmodifiableList(), merged).iterator();
            if (!iter.hasNext()) { throw new IllegalStateException(""); }
            return this.entityDeserializer.deserialize(iter.next());
        });
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
     * @since 0.3.3
     */
    @NotNull
    @Override
    public ResultCollection<E> create(@NotNull Iterable<E> entities) {
        return Result.ofTry(() -> SuccessCollection.of(Seq.of(entities).filter(Objects::nonNull)
            .mapAbort(entity -> this.create(Objects.requireNonNull(entity)).orAbort())
            .toUnmodifiableList()));
    }

    /**
     * {@inheritDoc}
     *
     * @param identifiers The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entity found in the form of an {@code Optional}.
     *
     * @see Optional
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Result<E> retrieveById(@NotNull Map<String, Object> identifiers) {
        return Result.ofThrowing(() -> {
            var iter = this.daoImpl.retrieve(
                this.entityMetadata.declaredIdentifiers().stream().map(EntityProperty::propertyName)
                    .collect(Collectors.toUnmodifiableList()), identifiers).iterator();
            return iter.hasNext() ? this.entityDeserializer.deserialize(iter.next())
                : Result.abort("Empty collection.");
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param props The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entities found in the form of an {@code Collection}.
     *
     * @see Collection
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public ResultCollection<E> retrieveByProps(@NotNull Map<String, Object> props) {
        return Result.ofTry(() -> SuccessCollection
            .of(Seq.of(() -> this.daoImpl.retrieve(props.keySet(), props))
                .mapThrowing(this.entityDeserializer::deserialize).mapAbort(Result::orAbort)
                .toUnmodifiableList()));
    }

    /**
     * {@inheritDoc}
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
    @Override
    public Result<E> retrieveFirstByProps(@NotNull Map<String, Object> properties) {
        return Result.ofThrowing(() -> {
            var iter = this.daoImpl.retrieve(
                this.entityMetadata.declaredIdentifiers().stream().map(EntityProperty::propertyName)
                    .collect(Collectors.toUnmodifiableList()), properties).iterator();
            return iter.hasNext() ? this.entityDeserializer.deserialize(iter.next())
                : Result.abort("Empty collection.");
        });
    }

    /**
     * {@inheritDoc}
     *
     * @return Returns the entities found in the form of a collection. The returned collection
     * should be immutable to avoid confusion about the synchronization behavior of the contained
     * entities with the data source.
     *
     * @see Collection
     * @since 0.3.3
     */
    @NotNull
    @Override
    public ResultCollection<E> retrieveAll() {
        return this.retrieveByProps(Map.of());
    }

    /**
     * {@inheritDoc}
     *
     * @param entity The immutable entity that should be updated.
     * @param props  The {@code Map} of key-value pairs that represents the properties and their
     *               updated values.
     *
     * @return Returns the updated entity.
     *
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Result<E> update(@NotNull E entity, @NotNull Map<String, Object> props) {
        return Result.ofThrowing(() -> {
            var components = this.entitySerializer.serialize(entity);
            this.daoImpl.update(components, props);
            var merged = this.entitySerializer.merge(components);
            var iter = this.daoImpl.retrieve(
                Seq.of(this.entityMetadata.declaredIdentifiers()).map(EntityProperty::propertyName)
                    .toUnmodifiableList(),
                Stream.of(merged, props).flatMap(map -> map.entrySet().stream()).collect(Collectors
                    .toUnmodifiableMap(Entry::getKey, Entry::getValue,
                        (oldEntry, newEntry) -> newEntry))).iterator();
            if (!iter.hasNext()) { throw new IllegalStateException(""); }
            return this.entityDeserializer.deserialize(iter.next());
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param entities The immutable entities that should be updated.
     * @param props    The {@code Iterable} of key-value pair {@code Map} objects that represents
     *                 the properties and their updated values.
     *
     * @return Returns the updated entity.
     *
     * @see Collection
     * @see Iterable
     * @see Map
     * @since 0.3.3
     */
    @NotNull
    @Override
    public ResultCollection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> props) {
        var res = Utils.zip(entities.iterator(), props.iterator())
            .map((Pair<E, Map<String, Object>> pair) -> this
                .update(Objects.requireNonNull(pair).first(), pair.second()))
            .collect(Collectors.toUnmodifiableList());

        return Result.ofTry(() -> SuccessCollection
            .of(Seq.of(res).mapAbort(el -> Objects.requireNonNull(el).orAbort())
                .toUnmodifiableList()));
    }

    /**
     * {@inheritDoc}
     *
     * @param entity The entity that should be deleted.
     *
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Result<Void> delete(@NotNull E entity) {
        try {
            return this.delete(this.entitySerializer
                .merge(this.entitySerializer.serialize(entity)));
        } catch (Throwable throwable) {
            return (Failure<Void>) () -> throwable;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param identifiers The identifiers that should be used to delete the entity.
     *
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Result<Void> delete(@NotNull Map<String, Object> identifiers) {
        return Result.ofThrowing(() -> this.daoImpl.delete(identifiers));
    }

    /**
     * {@inheritDoc}
     *
     * @param entities The {@code Iterable} of entities that should be deleted.
     *
     * @see Iterable
     * @since 0.3.3
     */
    @NotNull
    @Override
    public Result<Void> delete(@NotNull Iterable<E> entities) {
        return Result.ofTry(() -> Seq.of(entities).filter(Objects::nonNull)
            .mapAbort(entity -> this.delete(Objects.requireNonNull(entity)).orAbort())
            .toUnmodifiableList());
    }

    /**
     * {@inheritDoc}
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

    @NotNull
    @Contract(pure = true)
    public DaoImpl<Thr> daoImpl() {
        return this.daoImpl;
    }

}
