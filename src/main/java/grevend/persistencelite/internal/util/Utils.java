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

package grevend.persistencelite.internal.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Utils {

    private static final Set<String> arrayPrimitives =
        Set.of("void[]", "byte[]", "short[]", "int[]", "long[]", "float[]", "double[]", "boolean[]",
            "char[]");

    @Contract("null -> !null")
    @SuppressWarnings("unchecked")
    public static <A> String stringify(A a) {
        if (a == null) {
            return "null";
        } else {
            return a.getClass().isArray() &&
                !Utils.arrayPrimitives.contains(a.getClass().getCanonicalName()) ?
                Arrays.toString((A[]) a) : a.toString();
        }
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static @NotNull <T extends Number> T add(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer ca && b instanceof Integer cb) {
            return (T) (Integer) (ca + cb);
        } else if (a instanceof Double ca) {
            if (b instanceof Double cb) {
                return (T) (Double) (ca + cb);
            } else {
                return (T) (Double) (ca + ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static @NotNull <T extends Number> T sub(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer ca && b instanceof Integer cb) {
            return (T) (Integer) (ca - cb);
        } else if (a instanceof Double ca) {
            if (b instanceof Double cb) {
                return (T) (Double) (ca - cb);
            } else {
                return (T) (Double) (ca - ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Contract(pure = true)
    public static <T extends Number> boolean greaterThan(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer ca && b instanceof Integer cb) {
            return ca > cb;
        } else if (a instanceof Double ca) {
            if (b instanceof Double cb) {
                return ca > cb;
            } else {
                return (ca > ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Contract(pure = true)
    public static <T extends Number> boolean lessThan(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer ca && b instanceof Integer cb) {
            return ca < cb;
        } else if (a instanceof Double ca) {
            if (b instanceof Double cb) {
                return ca < cb;
            } else {
                return (ca < ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param key
     * @param map
     * @param maps
     * @param <T>
     * @param <R>
     *
     * @return
     *
     * @since 0.2.0
     */
    @Nullable
    public static <T, R> R extract(@Nullable T key, @NotNull Map<T, R> map, @NotNull Iterable<? extends Map<T, R>> maps) {
        R value = null;

        if (map.containsKey(key)) {
            value = map.get(key);
        } else {
            for (Map<T, R> next : maps) {
                if (next.containsKey(key)) {
                    map.put(key, (value = next.get(key)));
                    break;
                }
            }
        }

        return value;
    }

    public interface Pair<A, B> {
        A first();
        B second();
    }

    @NotNull
    public static <A, B> Stream<Pair<A, B>> zip(@NotNull BaseStream<A, Stream<A>> first, @NotNull BaseStream<B, Stream<B>> second) {
        return zip(first.iterator(), second.iterator());
    }

    @NotNull
    public static <A, B> Stream<Pair<A, B>> zip(@NotNull Iterator<A> first, @NotNull Iterator<B> second) {
        record PairImpl<A, B>(@Nullable A first, @Nullable B second) implements Pair<A, B> {}

        Iterable<Pair<A, B>> iter = () -> new Iterator<>() {

            @Override
            public boolean hasNext() {
                return first.hasNext() && second.hasNext();
            }

            @Override
            public Pair<A, B> next() {
                return new PairImpl<>(first.next(), second.next());
            }

        };

        return StreamSupport.stream(iter.spliterator(), false);
    }

}
