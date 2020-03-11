package grevend.persistence.lite.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<A extends Serializable, B extends Serializable> implements Serializable {

    private static final long serialVersionUID = 3602413341346015513L;

    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull <C extends Serializable, D extends Serializable> Pair<C, D> of(C c, D d) {
        return new Pair<>(c, d);
    }

    @Contract(pure = true)
    public static @NotNull <C extends Serializable, D extends Serializable> Collector<Pair<C, D>, ?, Map<C, D>> toMap() {
        return Collectors.toMap(Pair::getA, Pair::getB);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        return "Pair{"
                + "a=" + (a != null && a.getClass().isArray() ? Arrays.toString((A[]) a) : a)
                + ", b=" + (b != null && b.getClass().isArray() ? Arrays.toString((B[]) b) : b)
                + '}';
    }

}
