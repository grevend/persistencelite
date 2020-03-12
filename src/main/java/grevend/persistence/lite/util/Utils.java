package grevend.persistence.lite.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public class Utils {

  public static Set<String> arrayPrimitives =
      Set.of("void[]", "byte[]", "short[]", "int[]", "long[]", "float[]", "double[]", "boolean[]",
          "char[]");

  public static Predicate<Field> isFieldViable = field -> !field.isSynthetic()
      && !field.isAnnotationPresent(Ignore.class)
      && !Modifier.isAbstract(field.getModifiers())
      && !Modifier.isFinal(field.getModifiers())
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
