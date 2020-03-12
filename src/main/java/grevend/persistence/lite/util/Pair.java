package grevend.persistence.lite.util;

import grevend.persistence.lite.util.jacoco.Generated;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
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
        return this.a;
    }

    public B getB() {
        return this.b;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.getA(), pair.getA()) &&
                Objects.equals(this.getB(), pair.getB());
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.getA(), this.getB());
    }

    @Generated
    @Override
    public String toString() {
        return "Pair{"
                + "a=" + Utils.stringify(this.a)
                + ", b=" + Utils.stringify(this.b)
                + '}';
    }

}
