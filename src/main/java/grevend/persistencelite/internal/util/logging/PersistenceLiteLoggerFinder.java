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

import java.lang.System.Logger;

public final class PersistenceLiteLoggerFinder extends System.LoggerFinder {

    /**
     * Returns an instance of {@link Logger Logger} for the given {@code module}.
     *
     * @param name   the name of the logger.
     * @param module the module for which the logger is being requested.
     *
     * @return a {@link Logger logger} suitable for use within the given module.
     *
     * @throws NullPointerException if {@code name} is {@code null} or {@code module} is {@code
     *                              null}.
     * @throws SecurityException    if a security manager is present and its {@code checkPermission}
     *                              method doesn't allow the {@code RuntimePermission("loggerFinder")}.
     */
    @Override
    public Logger getLogger(String name, Module module) {
        return new PersistenceLiteLogger();
    }

}
