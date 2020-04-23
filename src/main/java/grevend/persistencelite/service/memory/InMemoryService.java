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

import grevend.persistencelite.dao.DaoFactory;
import grevend.persistencelite.dao.TransactionFactory;
import grevend.persistencelite.service.Service;
import grevend.persistencelite.util.TypeMarshaller;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @see Service
 * @see DaoFactory
 * @see TransactionFactory
 * @see TypeMarshaller
 * @since 0.2.0
 */
public final class InMemoryService implements Service<InMemoryConfigurator> {

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public InMemoryConfigurator getConfigurator() {
        return new InMemoryConfigurator(this);
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public DaoFactory getDaoFactory() {
        return new InMemoryDaoFactory();
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public TransactionFactory getTransactionFactory() {
        return new InMemoryTransactionFactory();
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
    public <A, B, E> void registerTypeMarshaller(@Nullable Class<E> entity, @NotNull Class<A> from, @NotNull Class<B> to, @NotNull TypeMarshaller<A, B> marshaller) {}

}
