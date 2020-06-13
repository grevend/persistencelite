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
import grevend.common.Failure;
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
import grevend.persistencelite.internal.service.rest.RestUtils;
import grevend.persistencelite.service.Service;
import grevend.persistencelite.util.TypeMarshaller;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<?, ?>>> marshallerMap;
    private final Map<Class<?>, Map<Class<?>, TypeMarshaller<?, ?>>> unmarshallerMap;
    private RestConfiguration configuration;

    public RestService() {
        this.marshallerMap = new HashMap<>();
        this.unmarshallerMap = new HashMap<>();
        RestUtils.initTypeMarshalling(this);
    }

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
    @Contract(value = " -> new", pure = true)
    public DaoFactory daoFactory() {
        if (this.configuration.mode() == RestMode.SERVER) {
            throw new IllegalStateException();
        }
        return new DaoFactory() {
            @NotNull
            @Override
            @SuppressWarnings("unchecked")
            public <E> Dao<E> createDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable Transaction transaction) {
                try {
                    return new RestDao<>(entityMetadata, new RestDaoImpl(entityMetadata,
                        "http://localhost:8000/api/v" + RestService.this.configuration.
                            version() + "/",
                        (Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>>)
                            (Object) RestService.this.marshallerMap,
                        (Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>>)
                            (Object) RestService.this.unmarshallerMap),
                        RestService.this.transactionFactory(), RestService.this.transactionFactory()
                        .createTransaction(), false, new HashMap<>(),
                        new HashMap<>());
                } catch (Throwable throwable) {
                    return new FailureDao<>(() -> throwable);
                }
            }
        };
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
        return this.daoFactory().createDao(EntityMetadata.of(entity), transaction);
    }

    /**
     * @param entity
     *
     * @return
     *
     * @since 0.4.7
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public <E> Dao<E> createDao(@NotNull Class<E> entity) {
        try {
            return this.createDao(entity, this.transactionFactory().createTransaction());
        } catch (Throwable throwable) {
            return new FailureDao<>((Failure<?>) () -> throwable);
        }
    }

    /**
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public TransactionFactory transactionFactory() {
        return () -> new Transaction() {

            @Override
            public void commit() throws Exception {}

            @Override
            public void rollback() throws Exception {}

            @Override
            public void close() throws Exception {}

        };
    }

    /**
     * @param entity
     * @param from
     * @param to
     * @param marshaller
     * @param unmarshaller
     * @param customNullHandling
     *
     * @since 0.5.2
     */
    @Override
    public <A, B, E> void registerTypeMarshaller(@Nullable Class<E> entity, @NotNull Class<A> from, @NotNull Class<B> to, @NotNull TypeMarshaller<A, B> marshaller, @NotNull TypeMarshaller<B, A> unmarshaller, boolean customNullHandling) {
        if (!this.marshallerMap.containsKey(entity)) {
            this.marshallerMap.put(entity, new HashMap<>());
        }
        if (!this.unmarshallerMap.containsKey(entity)) {
            this.unmarshallerMap.put(entity, new HashMap<>());
        }
        this.marshallerMap.get(entity).put(from, customNullHandling ? marshaller
            : (A a) -> a == null ? null : marshaller.marshall(a));
        this.unmarshallerMap.get(entity).put(from, customNullHandling ? unmarshaller
            : (B b) -> b == null ? null : unmarshaller.marshall(b));
    }

    /**
     * @param from
     * @param marshaller
     * @param unmarshaller
     * @param <A>
     *
     * @since 0.6.1
     */
    public <A> void registerTypeMarshaller(@NotNull Class<A> from, @NotNull TypeMarshaller<A, String> marshaller, @NotNull TypeMarshaller<String, A> unmarshaller) {
        this.registerTypeMarshaller(from, String.class, marshaller, unmarshaller);
    }

    /**
     * @param from
     * @param unmarshaller
     * @param <A>
     *
     * @since 0.6.1
     */
    public <A> void registerTypeMarshaller(@NotNull Class<A> from, @NotNull TypeMarshaller<String, A> unmarshaller) {
        this.registerTypeMarshaller(from, Object::toString, unmarshaller);
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
    @SuppressWarnings("unchecked")
    public HttpServer start() throws IOException {
        if (this.configuration.mode() != RestMode.SERVER && this.configuration.scope() != null) {
            throw new IllegalStateException();
        }

        HttpServer server = null;

        if (this.notEmpty("restHost") && this.notEmpty("restPort")) {
            try {
                server = HttpServer.create(new InetSocketAddress(Objects.requireNonNull(
                    this.configuration.properties()).getProperty("restHost"),
                        Integer.parseInt(Objects.requireNonNull(this.configuration.properties()).
                            getProperty("restPort"))),
                    this.configuration.backlog());
            } catch (NumberFormatException | NullPointerException exception) {
                server = HttpServer.create(new InetSocketAddress(8000),
                    this.configuration.backlog());
            }
        } else {
            server = HttpServer.create(new InetSocketAddress(8000),
                this.configuration.backlog());
        }

        RestHandler handler = new EntityHandler(this.configuration);
        HttpServer finalServer = server;

        EntityMetadata.entities(Objects.requireNonNull(this.configuration.scope())).forEach(
            entity -> finalServer.createContext("/api/v" + this.configuration.version() + "/" +
                entity.name().toLowerCase(), exchange -> {

                var headers = exchange.getResponseHeaders();
                headers.put("Content-Type", List.of("application/pl.v0.entity+json; utf-8"));
                headers.put("Last-Modified", List.of(DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(EntityHandler.lastModified
                        .computeIfAbsent(entity, e -> ZonedDateTime.now(ZoneOffset.UTC)))));
                handler.handle(this.configuration.version(), entity,
                    (Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>>)
                        (Object) this.marshallerMap,
                    (Map<Class<?>, Map<Class<?>, TypeMarshaller<Object, Object>>>)
                        (Object) this.unmarshallerMap, exchange);
            }));
        server.setExecutor(this.configuration.poolSize() < 1 ? null
            : Executors.newFixedThreadPool(this.configuration.poolSize()));
        server.start();
        return server;
    }

    private boolean notEmpty(@NotNull String property) {
        if (this.configuration.properties() != null) {
            var res = Objects.requireNonNull(this.configuration.properties()).getProperty(property);
            return res != null && !(res.equals(""));
        }
        return false;
    }

    @Contract("_ -> this")
    RestService setConfiguration(@NotNull RestConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

}
