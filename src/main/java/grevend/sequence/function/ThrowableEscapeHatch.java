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

package grevend.sequence.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @see ThrowingFunction
 * @see ThrowingConsumer
 * @since 0.2.3
 */
public final class ThrowableEscapeHatch<Thr extends Throwable> {

    private final Class<Thr> clazz;
    private Thr throwable;

    @Contract(pure = true)
    public ThrowableEscapeHatch(@NotNull Class<Thr> clazz) {
        this.clazz = clazz;
    }

    /**
     * @param function
     * @param escapeHatch
     * @param <T>
     * @param <R>
     *
     * @return
     *
     * @see ThrowingFunction
     * @since 0.2.3
     */
    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <T, R, Thr extends Throwable> Function<T, R> escape(@NotNull ThrowingFunction<T, R> function, @NotNull ThrowableEscapeHatch<Thr> escapeHatch) {
        return arg -> {
            try {
                return function.apply(arg);
            } catch (Throwable throwable) {
                if (!escapeHatch.getThrowableClass().isAssignableFrom(throwable.getClass())) {
                    throw new RuntimeException("Encountered unexpected throwable.", throwable);
                } else {
                    escapeHatch.escape((Thr) throwable);
                    return null;
                }
            }
        };
    }

    /**
     * @param consumer
     * @param escapeHatch
     * @param <T>
     *
     * @return
     *
     * @see ThrowingConsumer
     * @since 0.2.3
     */
    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <T, Thr extends Throwable> Consumer<T> escape(@NotNull ThrowingConsumer<T> consumer, @NotNull ThrowableEscapeHatch<Thr> escapeHatch) {
        return arg -> {
            try {
                consumer.accept(arg);
            } catch (Throwable throwable) {
                if (!escapeHatch.getThrowableClass().isAssignableFrom(throwable.getClass())) {
                    throw new RuntimeException("Encountered unexpected throwable.", throwable);
                } else {
                    escapeHatch.escape((Thr) throwable);
                }
            }
        };
    }

    /**
     * @param supplier
     * @param escapeHatch
     * @param <T>
     *
     * @return
     *
     * @see ThrowingSupplier
     * @since 0.2.3
     */
    @NotNull
    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static <T, Thr extends Throwable> Supplier<T> escape(@NotNull ThrowingSupplier<T> supplier, @NotNull ThrowableEscapeHatch<Thr> escapeHatch) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable throwable) {
                if (!escapeHatch.getThrowableClass().isAssignableFrom(throwable.getClass())) {
                    throw new RuntimeException("Encountered unexpected throwable.", throwable);
                } else {
                    escapeHatch.escape((Thr) throwable);
                    return null;
                }
            }
        };
    }

    /**
     * @return
     *
     * @since 0.2.3
     */
    @NotNull
    @Contract(pure = true)
    private Class<Thr> getThrowableClass() {
        return this.clazz;
    }

    /**
     * @param throwable
     *
     * @since 0.2.3
     */
    private void escape(@NotNull Thr throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
    }

    /**
     * @throws Thr
     * @since 0.2.3
     */
    @Contract(pure = true)
    public void rethrow() throws Thr {
        if (this.throwable != null) {
            throw this.throwable;
        }
    }

}
