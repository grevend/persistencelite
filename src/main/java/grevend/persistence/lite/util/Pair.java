package grevend.persistence.lite.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<A extends Serializable, B extends Serializable> implements Serializable {

    private static final long serialVersionUID = 3602413341346015513L;

    private final A a;
    private final B b;

    private Pair(A a, B b) {
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

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getA(), pair.getA()) &&
                Objects.equals(getB(), pair.getB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getA(), getB());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        return "Pair{"
                + "a=" +
                (a != null && a.getClass().isArray() &&
                        !Utils.arrayPrimitives.contains(a.getClass().getCanonicalName()) ?
                        Arrays.toString((A[]) a) : a)
                + ", b=" +
                (b != null && b.getClass().isArray() &&
                        !Utils.arrayPrimitives.contains(b.getClass().getCanonicalName()) ?
                        Arrays.toString((B[]) b) : b)
                + '}';
    }

}
