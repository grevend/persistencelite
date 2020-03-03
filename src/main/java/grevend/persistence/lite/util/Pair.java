package grevend.persistence.lite.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Pair<A, B> {

    private final A a;
    private final B b;

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

    public static @NotNull <C> Collector<C, ?, List<Pair<C, C>>> toPairs() {
        return toPairs(false);
    }

    public static @NotNull <C> Collector<C, ?, List<Pair<C, C>>> toPairs(boolean nullAsPlaceholder) {

        final class Pairing {

            private List<Pair<C, C>> pairs;

            private C first, second;
            private boolean empty;

            private Pairing() {
                this.pairs = new ArrayList<>();
                this.empty = true;
            }

            public void accept(C value) {
                if (empty) {
                    this.first = value;
                    this.empty = false;
                } else {
                    this.second = value;
                    this.pairs.add(Pair.of(first, second));
                    this.first = null;
                    this.second = null;
                    this.empty = true;
                }
            }

            @NotNull Pairing combine(@NotNull Pairing other) {
                if (!other.empty) {
                    this.accept(other.first);
                    this.second = other.second;
                }
                return this;
            }

            public List<Pair<C, C>> finish() {
                if (nullAsPlaceholder) {
                    if (this.first != null && this.second == null) {
                        this.pairs.add(Pair.of(this.first, null));
                    }
                }
                return this.pairs;
            }

        }

        return Collector.of(Pairing::new, Pairing::accept, Pairing::combine, Pairing::finish);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
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
                + "a=" + (a != null && a.getClass().isArray() ? Arrays.toString((A[]) a) : a)
                + ", b=" + (b != null && b.getClass().isArray() ? Arrays.toString((B[]) b) : b)
                + '}';
    }

}
