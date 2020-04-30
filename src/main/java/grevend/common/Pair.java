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

import grevend.persistencelite.internal.util.Utils;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record Pair<A extends Serializable, B extends Serializable>(@Nullable A first, B second) implements Serializable {

    private static final long serialVersionUID = 3602413341346015513L;

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull <A extends Serializable, B extends Serializable> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    @SuppressWarnings("unused")
    @Contract(pure = true)
    public static @NotNull <A extends Serializable, B extends Serializable> Collector<Pair<A, B>, ?, Map<A, B>> toMap() {
        return Collectors.toMap(Pair::first, Pair::second);
    }

    @NotNull
    @Override
    public String toString() {
        return "Pair{first=" + Utils.stringify(this.first) + ", second=" +
            Utils.stringify(this.second) + '}';
    }

}
