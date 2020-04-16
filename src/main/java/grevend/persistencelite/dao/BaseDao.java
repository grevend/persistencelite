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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseDao<E, T extends Transaction> implements Dao<E> {

    private final EntityMetadata<E> entityMetadata;
    private final T transaction;

    @Contract(pure = true)
    public BaseDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable T transaction) {
        this.entityMetadata = entityMetadata;
        this.transaction = transaction;
    }

    @Nullable
    protected T getTransaction() {
        return this.transaction;
    }

    @NotNull
    protected EntityMetadata<E> getEntityMetadata() {
        return this.entityMetadata;
    }

    @NotNull
    @Override
    public E create(@NotNull E entity) throws Exception {
        return this.create(entity, EntityFactory.deconstruct(this.entityMetadata, entity));
    }

    @NotNull
    @Override
    public Collection<E> create(@NotNull Iterable<E> entities) throws Exception {
        final var escapeHatch = new ExceptionEscapeHatch();
        var res = StreamSupport.stream(entities.spliterator(), false)
            .map(ExceptionEscapeHatch.escape(this::create, escapeHatch)).filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());
        escapeHatch.rethrow();
        return res;
    }

    @NotNull
    protected abstract E create(@NotNull E entity, @NotNull Collection<Map<String, Object>> properties) throws Exception;

    @NotNull
    @Override
    public Collection<E> retrieve(@NotNull Map<String, Object> properties) {
        return List.of();
    }

    @NotNull
    @Override
    public Collection<E> retrieve() {
        return null;
    }

    @NotNull
    @Override
    public E update(@NotNull E entity, @NotNull Map<String, Object> properties) {
        return null;
    }

    @NotNull
    @Override
    public Collection<E> update(@NotNull Iterable<E> entities, @NotNull Iterable<Map<String, Object>> properties) {
        return List.of();
    }

    @Override
    public void delete(@NotNull E entity) {

    }

    @Override
    public void delete(@NotNull Map<String, Object> properties) {

    }

    @Override
    public void delete(@NotNull Iterable<E> entities) {

    }

}
