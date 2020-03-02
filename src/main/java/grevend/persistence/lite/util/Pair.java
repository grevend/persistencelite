package grevend.persistence.lite.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<A, B> {

    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <C, D> Pair<C, D> of(C c, D d) {
        return new Pair<>(c, d);
    }

    @NotNull
    @Contract(pure = true)
    public static <C, D> Collector<Pair<C, D>, ?, Map<C, D>> toMap() {
        return Collectors.toMap(Pair::getA, Pair::getB);
    }

    @NotNull
    @Contract(pure = true)
    public static <C, D> Collector<Pair<C, D>, ?, ConcurrentMap<C, D>> toConcurrentMap() {
        return Collectors.toConcurrentMap(Pair::getA, Pair::getB);
    }

    public static @NotNull <C, D> Pair<C, D> unwrap(@NotNull Pair<Optional<C>, Optional<D>> pair)
            throws IllegalStateException {
        if (pair.getA().isPresent() && pair.getB().isPresent()) {
            return Pair.of(pair.getA().get(), pair.getB().get());
        } else {
            if (pair.getA().isEmpty() && pair.getB().isEmpty()) {
                throw new IllegalStateException("Optional values of <a> and <b> must be present.");
            } else if (pair.getA().isEmpty()) {
                throw new IllegalStateException("Optional value of <a> must be present.");
            } else if (pair.getB().isEmpty()) {
                throw new IllegalStateException("Optional value of <b> must be present.");
            } else {
                throw new IllegalStateException("Unwrap optional pair bug.");
            }
        }
    }

    public static @NotNull <C, D> Pair<C, D> unwrapA(@NotNull Pair<Optional<C>, D> pair) throws IllegalStateException {
        if (pair.getA().isPresent()) {
            return pair.withA(pair.getA().get());
        } else {
            throw new IllegalStateException("Optional value of <a> must be present.");
        }
    }

    public static @NotNull <C, D> Pair<C, D> unwrapB(@NotNull Pair<C, Optional<D>> pair) throws IllegalStateException {
        if (pair.getB().isPresent()) {
            return pair.withB(pair.getB().get());
        } else {
            throw new IllegalStateException("Optional value of <b> must be present.");
        }
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }

    public B getB() {
        return b;
    }

    public void setB(B b) {
        this.b = b;
    }

    public @NotNull <E> Pair<E, B> withA(E a) {
        return new Pair<>(a, this.b);
    }

    public @NotNull <E> Pair<A, E> withB(E b) {
        return new Pair<>(this.a, b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        return "Pair{"
                + "a=" + (a.getClass().isArray() ? Arrays.toString((A[]) a) : a)
                + ", b=" + (b.getClass().isArray() ? Arrays.toString((B[]) b) : b)
                + '}';
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

}
