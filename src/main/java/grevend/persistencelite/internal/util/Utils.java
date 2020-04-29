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

import grevend.persistencelite.entity.Ignore;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Utils {

    private static final Set<String> arrayPrimitives =
        Set.of("void[]", "byte[]", "short[]", "int[]", "long[]", "float[]", "double[]", "boolean[]",
            "char[]");
    public static Set<Class<?>> primitives = Set.of(
        Void.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
        Float.TYPE, Double.TYPE, Boolean.TYPE, Character.TYPE);
    public static Predicate<Field> isFieldViable = field -> !field.isSynthetic()
        && !field.isAnnotationPresent(Ignore.class)
        && !Modifier.isAbstract(field.getModifiers())
        && !Modifier.isStatic(field.getModifiers())
        && !Modifier.isTransient(field.getModifiers());
    public static Predicate<Constructor<?>> isConstructorViable =
        constructor -> constructor.getParameterCount() == 0 && !constructor.isSynthetic();

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
        if (a instanceof Integer && b instanceof Integer) {
            return (T) (Integer) (((Integer) a) + ((Integer) b));
        } else if (a instanceof Double) {
            if (b instanceof Double) {
                return (T) (Double) (((Double) a) + ((Double) b));
            } else {
                return (T) (Double) (((Double) a) + ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Contract(pure = true)
    @SuppressWarnings("unchecked")
    public static @NotNull <T extends Number> T sub(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer && b instanceof Integer) {
            return (T) (Integer) (((Integer) a) - ((Integer) b));
        } else if (a instanceof Double) {
            if (b instanceof Double) {
                return (T) (Double) (((Double) a) - ((Double) b));
            } else {
                return (T) (Double) (((Double) a) - ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Contract(pure = true)
    public static <T extends Number> boolean greaterThan(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer && b instanceof Integer) {
            return (((Integer) a) > ((Integer) b));
        } else if (a instanceof Double) {
            if (b instanceof Double) {
                return (((Double) a) > ((Double) b));
            } else {
                return (((Double) a) > ((Integer) b));
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Contract(pure = true)
    public static <T extends Number> boolean lessThan(@NotNull T a, @NotNull T b) {
        if (a instanceof Integer && b instanceof Integer) {
            return (((Integer) a) < ((Integer) b));
        } else if (a instanceof Double) {
            if (b instanceof Double) {
                return (((Double) a) < ((Double) b));
            } else {
                return (((Double) a) < ((Integer) b));
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

    /**
     * @param key
     * @param properties
     * @param resultSet
     * @param maps
     *
     * @return
     *
     * @throws SQLException
     * @since 0.2.0
     */
    @Nullable
    public static Object extract(@Nullable String key, @NotNull Collection<String> properties, @NotNull ResultSet resultSet, @NotNull Iterable<? extends Map<String, Object>> maps) throws SQLException {
        if (properties.contains(key)) {
            return resultSet.getObject(key);
        } else {
            for (Map<String, Object> next : maps) {
                if (next.containsKey(key)) {
                    return next.get(key);
                }
            }
        }
        return null;
    }

    /*public static void test(final Map<String, String> map) {
        var copy = new HashMap<>(map);
        System.out.println(extract("abcde", copy, List.of()));
        System.out.println(extract("bcde", copy, List.of()));
        System.out.println(extract("bcde", copy, List.of(Map.of("bcde", "12"))));
        System.out.println(
            extract("bcdecc", copy, List.of(Map.of("bcdef", "12"), Map.of("bcdecc", "21"))));

        System.out.println("Copy: " + copy);
        System.out.println(map);
    }

    public static void main(String[] args) {
        Map<String, String> test = new HashMap<>();
        test.put("abcde", "Hello!");
        System.out.println(test.get("abcde"));
        System.out.println("---------------");

        test(test);

        System.out.println("---------------");
        System.out.println(test);
    }*/

}
