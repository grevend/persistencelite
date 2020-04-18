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

package grevend.persistencelite.service.sql;

import grevend.persistencelite.dao.Dao;
import grevend.persistencelite.dao.DaoFactory;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.service.Service;
import grevend.persistencelite.service.sql.SqlDaoFactory;
import grevend.persistencelite.service.sql.SqlTransactionFactory;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PostgresService implements Service {

    @NotNull
    public <E> Dao<E> createDao(@NotNull Class<E> entity, @Nullable Transaction transaction) {
        return this.getDaoFactory().createDao(EntityMetadata.of(entity), transaction);
    }

    @NotNull
    public <E> Dao<E> createDao(@NotNull Class<E> entity) throws Exception {
        return this.createDao(entity, this.getTransactionFactory().createTransaction());
    }

    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public DaoFactory getDaoFactory() {
        return new SqlDaoFactory();
    }

    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public TransactionFactory getTransactionFactory() {
        return new SqlTransactionFactory(this::createConnection);
    }

    @NotNull
    private Connection createConnection() throws SQLException, URISyntaxException {
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "password");
        return DriverManager.getConnection("jdbc:postgresql://localhost/" + "postgres", props);
    }

}
