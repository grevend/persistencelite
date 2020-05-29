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

package grevend.persistencelite;

import grevend.persistencelite.service.Configurator;
import grevend.persistencelite.service.Service;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @since 0.2.0
 */
public final class PersistenceLite {

    public static final System.Logger LOGGER = System.getLogger("PersistenceLiteLogger");
    public static final String VERSION = "0.5.3";

    /**
     * @param service
     * @param <C>
     * @param <S>
     *
     * @return
     *
     * @since 0.3.0
     */
    @NotNull
    public static <C extends Configurator<S>, S extends Service<C>> C configure(@NotNull Class<S> service) {
        try {
            return service.getConstructor().newInstance().configurator();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Service configurator construction failed.", e);
        }
    }

}
