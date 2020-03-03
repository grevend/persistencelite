package grevend.persistence.lite.util;

import java.util.Arrays;

public class Triplet<A, B, C> {

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
    @SuppressWarnings("unchecked")
    public String toString() {
        return "Triplet{"
                + "a=" + (a.getClass().isArray() ? Arrays.toString((A[]) a) : a)
                + ", b=" + (b.getClass().isArray() ? Arrays.toString((B[]) b) : b)
                + ", c=" + (c.getClass().isArray() ? Arrays.toString((C[]) c) : c)
                + '}';
    }

}
