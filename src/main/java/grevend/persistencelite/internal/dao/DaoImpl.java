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

package grevend.persistencelite.internal.dao;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * @param <Thr>
 *
 * @author David Greven
 * @since 0.3.3
 */
public interface DaoImpl<Thr extends Throwable> {

    @NotNull
    Map<String, Object> create(@NotNull Iterable<Map<String, Object>> entity) throws Thr;

    @NotNull
    Map<String, Object> retrieveById(@NotNull Map<String, Object> identifiers) throws Thr;

    @NotNull
    Iterable<Map<String, Object>> retrieveByProps(@NotNull Map<String, Object> props) throws Thr;

    @NotNull
    Iterable<Map<String, Object>> retrieveAll() throws Thr;

    @NotNull
    Map<String, Object> update(@NotNull Map<String, Object> entity, @NotNull Map<String, Object> props) throws Thr;

    void delete(@NotNull Map<String, Object> props) throws Thr;

    void delete(@NotNull Iterable<Map<String, Object>> entities) throws Thr;

}
