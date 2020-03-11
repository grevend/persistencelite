package grevend.persistence.lite.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Triplet<A extends Serializable, B extends Serializable, C extends Serializable> implements Serializable {

    private static final long serialVersionUID = 2550349264294704474L;

    private final A a;
    private final B b;
    private final C c;

    public Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equals(getA(), triplet.getA()) &&
                Objects.equals(getB(), triplet.getB()) &&
                Objects.equals(getC(), triplet.getC());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getA(), getB(), getC());
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        return "Triplet{"
                + "a=" + (a != null && a.getClass().isArray() &&
                !Utils.arrayPrimitives.contains(a.getClass().getCanonicalName()) ? Arrays.toString((A[]) a) : a)
                + ", b=" + (b != null && b.getClass().isArray() &&
                !Utils.arrayPrimitives.contains(b.getClass().getCanonicalName()) ? Arrays.toString((B[]) b) : b)
                + ", c=" + (c != null && c.getClass().isArray() &&
                !Utils.arrayPrimitives.contains(c.getClass().getCanonicalName()) ? Arrays.toString((C[]) c) : c)
                + '}';
    }

}
