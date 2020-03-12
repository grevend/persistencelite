package grevend.persistence.lite.util;

import grevend.persistence.lite.util.jacoco.Generated;

import java.io.Serializable;
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
        return this.a;
    }

    public B getB() {
        return this.b;
    }

    public C getC() {
        return this.c;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equals(this.getA(), triplet.getA()) &&
                Objects.equals(this.getB(), triplet.getB()) &&
                Objects.equals(this.getC(), triplet.getC());
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.getA(), this.getB(), this.getC());
    }

    @Override
    public String toString() {
        return "Triplet{"
                + "a=" + Utils.stringify(this.a)
                + ", b=" + Utils.stringify(this.b)
                + ", c=" + Utils.stringify(this.c)
                + '}';
    }

}
