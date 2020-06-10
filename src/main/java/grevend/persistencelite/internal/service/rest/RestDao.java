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

package grevend.persistencelite.internal.service.rest;

import grevend.persistencelite.PersistenceLite;
import grevend.persistencelite.dao.Transaction;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.BaseDao;
import grevend.persistencelite.util.TypeMarshaller;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @param <E>
 *
 * @author David Greven
 * @since 0.4.7
 */
public final class RestDao<E> extends BaseDao<E, Throwable> {

    private final RestDaoImpl daoImpl;
    private final EntityMetadata<E> entityMetadata;

    /**
     * @param entityMetadata
     * @param daoImpl
     * @param transactionFactory
     * @param transaction
     * @param props
     *
     * @throws Throwable
     * @since 0.4.7
     */
    public RestDao(@NotNull EntityMetadata<E> entityMetadata, @NotNull RestDaoImpl daoImpl, @NotNull TransactionFactory transactionFactory, @Nullable Transaction transaction, boolean props, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<?, ?>>> marshallerMap, @NotNull Map<Class<?>, Map<Class<?>, TypeMarshaller<?, ?>>> unmarshallerMap) throws Throwable {
        super(entityMetadata, daoImpl, transactionFactory, transaction, props, marshallerMap,
            unmarshallerMap);
        this.daoImpl = daoImpl;
        this.entityMetadata = entityMetadata;
    }

    /**
     * Returns the last modification timestamp for the entity type assigned to this dao. The result
     * is only available if an operation was previously carried out which contained this information
     * in the response header.
     *
     * @param head If head is set to true, a HEAD request is send to get the most current value.
     *
     * @return Returns the timestamp as a {@link ZonedDateTime}.
     *
     * @see ZonedDateTime
     * @since 0.4.7
     */
    @Nullable
    @Contract(pure = true)
    public ZonedDateTime lastModified(boolean head) {
        if (head) {
            try {
                var conn = this.daoImpl.connection();
                conn.setRequestProperty("Accept-Charset", "utf-8");
                conn.setRequestProperty("User-Agent", "PersistenceLite/" + PersistenceLite.VERSION +
                    " (Java/" + Runtime.version() + ") Cache/v0");
                this.daoImpl.lastModified = ZonedDateTime
                    .parse(conn.getHeaderField("Last-Modified"),
                        DateTimeFormatter.RFC_1123_DATE_TIME);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return this.daoImpl.lastModified;
    }

    /**
     * Returns the last modification timestamp for the entity type assigned to this dao. The result
     * is only available if an operation was previously carried out which contained this information
     * in the response header.
     *
     * @return Returns the timestamp as a {@link ZonedDateTime}.
     *
     * @see ZonedDateTime
     * @since 0.6.4
     */
    @Contract(pure = true)
    @Nullable
    @SuppressWarnings("unused")
    public ZonedDateTime lastModified() {
        return this.lastModified(false);
    }

}
