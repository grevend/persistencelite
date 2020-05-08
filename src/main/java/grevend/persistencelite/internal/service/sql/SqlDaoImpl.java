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

import grevend.persistencelite.crud.Crud;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.internal.util.Utils;
import grevend.sequence.function.ThrowableEscapeHatch;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @param <E>
 *
 * @author David Greven
 * @since 0.3.3
 */
public final record SqlDaoImpl<E>(@NotNull EntityMetadata<E>entityMetadata, @NotNull SqlTransaction transaction, @NotNull Supplier<Transaction>transactionSupplier, @NotNull PreparedStatementFactory preparedStatementFactory) implements DaoImpl<SQLException> {

    @Contract(pure = true)
    public SqlDaoImpl(@NotNull EntityMetadata<E> entityMetadata, @NotNull SqlTransaction transaction, @NotNull Supplier<Transaction> transactionSupplier) {
        this(entityMetadata, transaction, transactionSupplier, new PreparedStatementFactory());
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws SQLException {
        final var escapeHatch = new ThrowableEscapeHatch<>(SQLException.class);

        Utils.zip(this.entityMetadata.superTypes().iterator(), entity.iterator())
            .filter(Objects::nonNull).forEach(ThrowableEscapeHatch.escapeSuper(
            pair -> this.preparedStatementFactory.values(Objects.requireNonNull(pair).first(),
                Objects.requireNonNull(this.preparedStatementFactory
                    .prepare(Crud.CREATE, pair.first(), this.transaction, true, -1)), pair.second())
                .executeUpdate(), escapeHatch));

        escapeHatch.rethrow();
    }

    @NotNull
    @Override
    public Iterable<Map<String, Object>> retrieve(@NotNull Map<String, Object> props) throws SQLException {
        return null;
    }

    @Override
    public void update(@NotNull Map<String, Object> entity, @NotNull Map<String, Object> props) throws SQLException {

    }

    @Override
    public void delete(@NotNull Map<String, Object> props) throws SQLException {

    }

}
