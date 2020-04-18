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

package grevend.persistencelite.service.memory;

import grevend.persistencelite.dao.BaseDao;
import grevend.persistencelite.entity.EntityMetadata;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <E> The type of the entity to which this DAO implementation should apply.
 *
 * @author David Greven
 * @version 0.2.0
 * @see BaseDao
 * @see InMemoryTransaction
 */
public class InMemoryDao<E> extends BaseDao<E, InMemoryTransaction> {

    /**
     * @param entityMetadata
     * @param transaction
     *
     * @see EntityMetadata
     * @see InMemoryTransaction
     * @since 0.2.0
     */
    public InMemoryDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable InMemoryTransaction transaction) {
        super(entityMetadata, transaction);
    }

    /**
     * @param entity
     * @param properties
     *
     * @return
     *
     * @throws Exception
     * @see Collection
     * @see Map
     * @since 0.2.0
     */
    @NotNull
    @Override
    protected E create(@NotNull E entity, @NotNull Collection<Map<String, Object>> properties) throws Exception {
        return null;
    }

    /**
     * An implementation of the <b>retrieve</b> CRUD operation which returns all matching entities
     * based on the key-value pairs passed as parameters in the form of a {@code Map}.
     *
     * @param identifiers The key-value pairs in the form of a {@code Map}.
     *
     * @return Returns the entity found in the form of an {@code Optional}.
     *
     * @throws Exception If an error occurs during the persistence process.
     * @see Optional
     * @see Map
     * @since 0.2.0
     */
    @Override
    public @NotNull Optional<E> retrieve(@NotNull Map<String, Object> identifiers) throws Exception {
        return Optional.empty();
    }

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
    @Override
    public @NotNull Collection<E> retrieve() throws Exception {
        return null;
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
