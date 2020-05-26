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

package grevend.persistencelite.service.rest;

import com.sun.net.httpserver.HttpServer;
import grevend.persistencelite.dao.Dao;
import grevend.persistencelite.dao.DaoFactory;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.FailureDao;
import grevend.persistencelite.internal.service.rest.EntityHandler;
import grevend.persistencelite.internal.service.rest.RestConfiguration;
import grevend.persistencelite.internal.service.rest.RestDao;
import grevend.persistencelite.internal.service.rest.RestDaoImpl;
import grevend.persistencelite.internal.service.rest.RestHandler;
import grevend.persistencelite.internal.util.Utils;
import grevend.persistencelite.service.Service;
import grevend.persistencelite.util.TypeMarshaller;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see RestConfigurator
 * @since 0.3.0
 */
public final class RestService implements Service<RestConfigurator> {

    private RestConfiguration configuration;

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public RestConfigurator configurator() {
        return new RestConfigurator(this);
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    public DaoFactory daoFactory() {
        return Objects.requireNonNull(this.configuration.service()).daoFactory();
    }

    /**
     * @param entity
     * @param transaction
     *
     * @return
     *
     * @since 0.4.7
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public <E> Dao<E> createDao(@NotNull Class<E> entity, @Nullable Transaction transaction) {
        try {
            return new RestDao<>(EntityMetadata.of(entity),
                new RestDaoImpl(EntityMetadata.of(entity)),
                this.transactionFactory(), transaction, true);
        } catch (Throwable throwable) {
            return new FailureDao<>(() -> throwable);
        }
    }

    /**
     * @param entity
     *
     * @return
     *
     * @since 0.4.7
     */
    @Override
    @Contract(pure = true)
    public @NotNull <E> Dao<E> createDao(@NotNull Class<E> entity) {
        try {
            return new RestDao<>(EntityMetadata.of(entity),
                new RestDaoImpl(EntityMetadata.of(entity)), this.transactionFactory(),
                this.transactionFactory().createTransaction(), true);
        } catch (Throwable throwable) {
            return new FailureDao<>(() -> throwable);
        }
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    public TransactionFactory transactionFactory() {
        return Objects.requireNonNull(this.configuration.service()).transactionFactory();
    }

    /**
     * @param entity
     * @param from
     * @param to
     * @param marshaller
     *
     * @since 0.3.0
     */
    @Override
    public <A, B, E> void registerTypeMarshaller(@Nullable Class<E> entity, @NotNull Class<A> from, @NotNull Class<B> to, @NotNull TypeMarshaller<A, B> marshaller) {
        Objects.requireNonNull(this.configuration.service())
            .registerTypeMarshaller(entity, from, to, marshaller);
    }

    /**
     * @return
     *
     * @since 0.4.5
     */
    @Override
    @Contract(pure = true)
    public boolean allowsCaching() {
        return true;
    }

    /**
     * @since 0.3.3
     */
    @NotNull
    public HttpServer start() throws IOException {
        if (this.configuration.mode() != RestMode.SERVER && this.configuration.scope() != null) {
            throw new IllegalStateException();
        }

        var server = HttpServer.create(new InetSocketAddress(8000), 0);
        RestHandler handler = new EntityHandler(this.configuration);

        EntityMetadata.entities(Objects.requireNonNull(this.configuration.scope())).forEach(
            entity -> server.createContext(
                "/api/v" + this.configuration.version() + "/" + entity.name().toLowerCase(),
                exchange -> {
                    var headers = exchange.getResponseHeaders();
                    headers.put("Content-Type", List.of("application/pl.v0.entity+json; utf-8"));
                    headers.put("Last-Modified", List.of(DateTimeFormatter.RFC_1123_DATE_TIME
                        .format(EntityHandler.lastModified
                            .computeIfAbsent(entity, e -> ZonedDateTime.now(ZoneOffset.UTC)))));
                    handler.handle(exchange.getRequestURI(), exchange.getRequestMethod(),
                        Utils.query(exchange.getRequestURI()), this.configuration.version(), entity,
                        exchange);
                }));
        server.setExecutor(this.configuration.poolSize() < 1 ? null
            : Executors.newFixedThreadPool(this.configuration.poolSize()));
        server.start();
        return server;
    }

    @Contract("_ -> this")
    RestService setConfiguration(@NotNull RestConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

}
