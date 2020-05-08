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

package grevend.persistencelite.service;

import grevend.persistencelite.dao.DaoFactory;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.util.TypeMarshaller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see DaoFactory
 * @see TransactionFactory
 * @see TypeMarshaller
 * @since 0.2.0
 */
public interface Service<C extends Configurator<? extends Service<C>>> {

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    C configurator();

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    DaoFactory daoFactory();

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    TransactionFactory transactionFactory();

    /**
     * @param from
     * @param to
     * @param marshaller
     * @param <A>
     * @param <B>
     *
     * @since 0.2.0
     */
    default <A, B> void registerTypeMarshaller(Class<A> from, Class<B> to, TypeMarshaller<A, B> marshaller) {
        this.registerTypeMarshaller(null, from, to, marshaller);
    }

    /**
     * @param entity
     * @param from
     * @param to
     * @param marshaller
     * @param <A>
     * @param <B>
     * @param <E>
     *
     * @since 0.2.0
     */
    <A, B, E> void registerTypeMarshaller(@Nullable Class<E> entity, @NotNull Class<A> from, @NotNull Class<B> to, @NotNull TypeMarshaller<A, B> marshaller);

}
