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

package grevend.persistencelite.internal.util.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class PersistenceLiteLogger implements System.Logger {

    /**
     * Returns the name of this logger.
     *
     * @return the logger name.
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public String getName() {
        return "PersistenceLiteLogger";
    }

    /**
     * Checks if a message of the given level would be logged by this logger.
     *
     * @param level the log message level.
     *
     * @return {@code true} if the given log message level is currently being logged.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     */
    @Override
    @Contract(pure = true)
    public boolean isLoggable(Level level) {
        return true;
    }

    /**
     * Logs a localized message associated with a given throwable.
     * <p>
     * If the given resource bundle is non-{@code null},  the {@code msg} string is localized using
     * the given resource bundle. Otherwise the {@code msg} string is not localized.
     *
     * @param level  the log message level.
     * @param bundle a resource bundle to localize {@code msg}; can be {@code null}.
     * @param msg    the string message (or a key in the message catalog, if {@code bundle} is not
     *               {@code null}); can be {@code null}.
     * @param thrown a {@code Throwable} associated with the log message; can be {@code null}.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     */
    @Override
    public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
        System.out.printf("PersistenceLite [%s]: %s - %s%n", level, msg, thrown);
    }

    /**
     * Logs a message with resource bundle and an optional list of parameters.
     * <p>
     * If the given resource bundle is non-{@code null},  the {@code format} string is localized
     * using the given resource bundle. Otherwise the {@code format} string is not localized.
     *
     * @param level  the log message level.
     * @param bundle a resource bundle to localize {@code format}; can be {@code null}.
     * @param format the string message format in {@link MessageFormat} format, (or a key in the
     *               message catalog if {@code bundle} is not {@code null}); can be {@code null}.
     * @param params an optional list of parameters to the message (may be none).
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     */
    @Override
    public void log(Level level, ResourceBundle bundle, String format, Object... params) {
        System.out
            .printf("PersistenceLite [%s]: %s%n", level, MessageFormat.format(format, params));
    }

}
