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

package grevend.common;

import grevend.jacoco.Generated;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author David Greven
 * @since 0.2.0
 */
public class Lazy<E> {

    private final Supplier<E> supplier;

    private volatile E element;

    /**
     * @param supplier
     *
     * @since 0.2.0
     */
    @Contract(pure = true)
    public Lazy(@NotNull final Supplier<E> supplier) {
        this.supplier = supplier;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @Nullable
    public synchronized E get() {
        if (this.element == null) {
            this.element = this.supplier.get();
        }
        return this.element;
    }

    /**
     * @param o
     *
     * @return
     *
     * @since 0.2.0
     */
    @Override
    @Generated
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || this.getClass() != o.getClass()) { return false; }
        Lazy<?> lazy = (Lazy<?>) o;
        return Objects.equals(this.supplier, lazy.supplier) &&
            Objects.equals(this.element, lazy.element);
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.supplier, this.element);
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    public String toString() {
        return "Lazy{element=" + this.element + '}';
    }

}
