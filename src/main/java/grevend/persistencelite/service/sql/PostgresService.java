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
import grevend.persistencelite.util.TypeMarshaller;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see Service
 * @since 0.2.0
 */
public final class PostgresService implements Service<PostgresConfigurator> {

    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<?, ?>>> marshallerMap;
    private Properties properties;

    /**
     * @since 0.2.0
     */
    @Contract(pure = true)
    public PostgresService() {
        this.marshallerMap = new HashMap<>();
        this.properties = new Properties();
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Contract(pure = true)
    public Map<Class<?>, Map<Class<?>, TypeMarshaller<?, ?>>> getMarshallerMap() {
        return this.marshallerMap;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Contract(pure = true)
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * @param properties
     *
     * @since 0.2.0
     */
    public void setProperties(@NotNull Properties properties) {
        this.properties = properties;
    }

    /**
     * @param entity
     * @param transaction
     * @param <E>
     *
     * @return
     *
     * @see Dao
     * @see Class
     * @see Transaction
     * @since 0.2.0
     */
    @NotNull
    public <E> Dao<E> createDao(@NotNull Class<E> entity, @Nullable Transaction transaction) {
        return this.getDaoFactory().createDao(EntityMetadata.of(entity), transaction);
    }

    /**
     * @param entity
     * @param <E>
     *
     * @return
     *
     * @throws Exception
     * @see Dao
     * @see Class
     * @since 0.2.0
     */
    @NotNull
    public <E> Dao<E> createDao(@NotNull Class<E> entity) throws Exception {
        return this.createDao(entity, this.getTransactionFactory().createTransaction());
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public PostgresConfigurator getConfigurator() {
        return new PostgresConfigurator(this);
    }

    /**
     * @return
     *
     * @see DaoFactory
     * @see SqlDaoFactory
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public DaoFactory getDaoFactory() {
        return new SqlDaoFactory();
    }

    /**
     * @return
     *
     * @see TransactionFactory
     * @see SqlTransactionFactory
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public TransactionFactory getTransactionFactory() {
        return new SqlTransactionFactory(this::createConnection);
    }

    /**
     * @param entity
     * @param from
     * @param to
     * @param marshaller
     *
     * @since 0.2.0
     */
    @Override
    @Contract(pure = true)
    public <A, B, E> void registerTypeMarshaller(@Nullable Class<E> entity, @NotNull Class<A> from, @NotNull Class<B> to, @NotNull TypeMarshaller<A, B> marshaller) {
        if (!this.marshallerMap.containsKey(entity)) {
            this.marshallerMap.put(entity, new HashMap<>());
        }
        this.marshallerMap.get(entity).put(from, marshaller);
    }

    /**
     * @return
     *
     * @throws SQLException
     * @see Connection
     * @since 0.2.0
     */
    @NotNull
    private Connection createConnection() throws SQLException {
        if (this.properties.getProperty("user") == null
            || this.properties.getProperty("password") == null) {
            throw new IllegalStateException("No credentials provided.");
        }

        return DriverManager
            .getConnection("jdbc:postgresql://localhost/" + "postgres", this.properties);
    }

}
