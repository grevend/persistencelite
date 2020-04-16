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

package grevend.persistencelite.util;

import grevend.persistencelite.util.function.ThrowingFunction;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ExceptionEscapeHatch {

    private Exception exception;

    @NotNull
    @Contract(pure = true)
    public static <T, R> Function<T, R> escape(@NotNull ThrowingFunction<T, R> function, @NotNull ExceptionEscapeHatch exceptionEscapeHatch) {
        return arg -> {
            try {
                return function.apply(arg);
            } catch (Exception exception) {
                exceptionEscapeHatch.escape(exception);
                return null;
            }
        };
    }

    private void escape(@NotNull Exception exception) {
        if (this.exception == null) {
            this.exception = exception;
        }
    }

    public void rethrow() throws Exception {
        if (this.exception != null) {
            throw this.exception;
        }
    }

}
