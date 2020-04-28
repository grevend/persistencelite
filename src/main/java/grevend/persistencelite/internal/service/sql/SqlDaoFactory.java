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

import grevend.persistencelite.dao.Dao;
import grevend.persistencelite.dao.DaoFactory;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.entity.EntityMetadata;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see DaoFactory
 * @see Dao
 * @see EntityMetadata
 * @see Transaction
 * @since 0.2.0
 */
public final class SqlDaoFactory implements DaoFactory {

    private final Supplier<Transaction> transactionSupplier;

    @Contract(pure = true)
    public SqlDaoFactory(@NotNull Supplier<Transaction> transactionSupplier) {
        this.transactionSupplier = transactionSupplier;
    }

    /**
     * @param entityMetadata
     * @param transaction
     * @param <E>
     *
     * @return
     *
     * @see Dao
     * @see EntityMetadata
     * @see Transaction
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract("_, null -> fail")
    public <E> Dao<E> createDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable Transaction transaction) {
        if (transaction instanceof SqlTransaction sqlTransaction) {
            EntityMetadata.inferRelationTypes(entityMetadata);
            return new SqlDao<>(entityMetadata, sqlTransaction, transactionSupplier);
        } else {
            throw new IllegalArgumentException("Transaction must be of type SqlTransaction");
        }
    }

}
