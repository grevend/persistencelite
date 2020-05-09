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

import grevend.sequence.function.ThrowingRunnable;
import grevend.sequence.function.ThrowingSupplier;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Result<T> {

    @NotNull
    @Contract(pure = true)
    static <T> Result<T> of(@NotNull Failure<?> failure) {
        return (Failure<T>) failure::fail;
    }

    @NotNull
    @Contract(value = "_ -> param1", pure = true)
    static <T> Result<T> of(@NotNull Success<T> success) {
        return success;
    }

    @NotNull
    static <T> Result<T> ofThrowing(@NotNull ThrowingSupplier<T> supplier) {
        try {
            var value = supplier.get();
            return (Success<T>) () -> value;
        } catch (Throwable throwable) {
            return (Failure<T>) () -> throwable;
        }
    }

    @NotNull
    static Result<Void> ofThrowing(@NotNull ThrowingRunnable runnable) {
        try {
            runnable.run();
            return (Success<Void>) () -> null;
        } catch (Throwable throwable) {
            return (Failure<Void>) () -> throwable;
        }
    }

    boolean success();

    default boolean failure() {
        return !this.success();
    }

    T or(T or);

    T orThrow() throws Throwable;

    @Nullable
    default T orNull() {
        return this.or(null);
    }

    @NotNull
    default Optional<T> toOptional() {
        return this.failure() ? Optional.empty() : Optional.ofNullable(this.orNull());
    }

}
