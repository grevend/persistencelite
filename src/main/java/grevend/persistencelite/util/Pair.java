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

import grevend.jacoco.Generated;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Pair<A extends Serializable, B extends Serializable> implements Serializable {

    private static final long serialVersionUID = 3602413341346015513L;

    private final A a;
    private final B b;

    @Contract(pure = true)
    private Pair(@Nullable A a, @Nullable B b) {
        this.a = a;
        this.b = b;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull <A extends Serializable, B extends Serializable> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    @Contract(pure = true)
    public static @NotNull <A extends Serializable, B extends Serializable> Collector<Pair<A, B>, ?, Map<A, B>> toMap() {
        return Collectors.toMap(Pair::getA, Pair::getB);
    }

    public @Nullable A getA() {
        return this.a;
    }

    public @Nullable B getB() {
        return this.b;
    }

    @Override
    @Generated
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.getA(), pair.getA()) &&
            Objects.equals(this.getB(), pair.getB());
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.getA(), this.getB());
    }

    @Override
    public String toString() {
        return "Pair{"
            + "a=" + Utils.stringify(this.a)
            + ", b=" + Utils.stringify(this.b)
            + '}';
    }

}
