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

package grevend.persistence.lite.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public class Utils {

  public static Set<Class<?>> primitives = Set.of(
      Void.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
      Float.TYPE, Double.TYPE, Boolean.TYPE, Character.TYPE);
  public static Set<String> arrayPrimitives =
      Set.of("void[]", "byte[]", "short[]", "int[]", "long[]", "float[]", "double[]", "boolean[]",
          "char[]");
  public static Predicate<Field> isFieldViable = field -> !field.isSynthetic()
      && !field.isAnnotationPresent(Ignore.class)
      && !Modifier.isAbstract(field.getModifiers())
      && !Modifier.isStatic(field.getModifiers())
      && !Modifier.isTransient(field.getModifiers());
  public static Predicate<Constructor<?>> isConstructorViable =
      constructor -> constructor.getParameterCount() == 0 && !constructor.isSynthetic();

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

}
