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

package grevend.sequence;

import grevend.common.Failure;
import grevend.common.Result;
import grevend.common.Result.AbortOnFailure;
import grevend.common.Success;
import grevend.persistencelite.internal.util.Utils;
import grevend.sequence.function.ThrowableEscapeHatch;
import grevend.sequence.function.ThrowingFunction;
import grevend.sequence.function.ThrowingSupplier;
import grevend.sequence.function.TriFunction;
import grevend.sequence.iterators.ConcatIterator;
import grevend.sequence.iterators.DistinctIterator;
import grevend.sequence.iterators.FilterIterator;
import grevend.sequence.iterators.FlatMapIterator;
import grevend.sequence.iterators.GeneratorIterator;
import grevend.sequence.iterators.GeneratorWithSeedIterator;
import grevend.sequence.iterators.LimitIterator;
import grevend.sequence.iterators.MapIterator;
import grevend.sequence.iterators.MergeIterator;
import grevend.sequence.iterators.PeekIterator;
import grevend.sequence.iterators.RangeIterator;
import grevend.sequence.iterators.SkipIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Seq<T, S extends Seq<T, S>> implements Iterable<T> {

    protected Iterator<T> iterator;

    @Contract(pure = true)
    Seq(@NotNull Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Contract(" -> new")
    public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> empty() {
        return new Seq<>(new Iterator<>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                return null;
            }

        });
        //return new EmptySeq<T>();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(@NotNull Iterator<T> iterator) {
        return new Seq<>(iterator);
    }

    @Contract("_ -> new")
    public static @NotNull <T extends Number, S extends NumberSeq<T>> NumberSeq<T> ofNumeric(
        @NotNull Iterator<T> iterator) {
        return new NumberSeq<>(iterator);
    }

    public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(@NotNull Iterable<T> iterable) {
        return of(iterable.iterator());
    }

    public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(@NotNull ThrowingSupplier<Iterable<T>> supplier) {
        try {
            return of(Objects.requireNonNull(supplier.get()).iterator());
        } catch (Throwable throwable) {
            return Seq.empty();
        }
    }

    public static @NotNull <T extends Number, S extends NumberSeq<T>> NumberSeq<T> ofNumeric(
        @NotNull Iterable<T> iterable) {
        return ofNumeric(iterable.iterator());
    }

    @SafeVarargs
    public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(T... values) {
        return of(Arrays.asList(values));
    }

    @Contract("_ -> new")
    @SafeVarargs
    public static @NotNull <T extends Number> NumberSeq<T> of(T... values) {
        return new NumberSeq<>(Arrays.asList(values).iterator());
    }

    @Contract("_ -> new")
    @SafeVarargs
    public static @NotNull <T extends Number> NumberSeq<T> ofNumbers(T... values) {
        return new NumberSeq<>(Arrays.asList(values).iterator());
    }

    public static @NotNull <T, S extends Seq<T, S>, S2> Seq<T, S> range(@NotNull T startInclusive,
        @NotNull T endInclusive, @NotNull TriFunction<T, T, S2, T> stepper, @NotNull S2 stepSize) {
        return of(new RangeIterator<>(startInclusive, endInclusive, stepper, stepSize));
    }

    @Contract("_, _, _, _ -> new")
    public static @NotNull <T extends Number> NumberSeq<T> range(@NotNull T startInclusive,
        @NotNull T endInclusive, @NotNull TriFunction<T, T, T, T> stepper, @NotNull T stepSize) {
        return new NumberSeq<>(
            new RangeIterator<>(startInclusive, endInclusive, stepper, stepSize));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull <T extends Number> NumberSeq<T> range(@NotNull T startInclusive,
        @NotNull T endInclusive, @NotNull T stepSize) {
        return new NumberSeq<>(new RangeIterator<>(startInclusive, endInclusive,
            Utils.lessThan(startInclusive, endInclusive) ? (current, end, step) -> {
                var value = Utils.add(current, step);
                return Utils.greaterThan(value, end) ? end : value;
            } : (current, end, step) -> {
                var value = Utils.sub(current, step);
                return Utils.lessThan(value, end) ? end : value;
            }, stepSize));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <T extends Number> NumberSeq<T> range(@NotNull T startInclusive,
        @NotNull T endInclusive) {
        return range(startInclusive, endInclusive, (T) (Number) 1);
    }

    public static @NotNull <S extends Seq<Character, S>> Seq<Character, S> range(char startInclusive,
        char endInclusive, int stepSize) {
        return range((int) startInclusive, (int) endInclusive, stepSize)
            .map(character -> (char) character.intValue());
    }

    public static @NotNull <S extends Seq<Character, S>> Seq<Character, S> range(char startInclusive,
        char endInclusive) {
        return range(startInclusive, endInclusive, 1);
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <T, S extends Seq<T, S>> S generate(@NotNull Supplier<T> supplier) {
        return (S) of(new GeneratorIterator<>(supplier));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <T, S extends Seq<T, S>> S generate(@NotNull T seed,
        @NotNull UnaryOperator<T> function) {
        return (S) of(new GeneratorWithSeedIterator<>(seed, function));
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.iterator;
    }

    @SuppressWarnings("unchecked")
    public @NotNull S filter(@NotNull Predicate<? super T> predicate) {
        this.iterator = new FilterIterator<>(this.iterator, predicate);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull <R, U extends Seq<R, U>> U map(
        @NotNull Function<? super T, ? extends R> function) {
        return (U) Seq.<R, U>of(new MapIterator<>(this.iterator, function));
    }

    @SuppressWarnings("unchecked")
    public @NotNull <R, U extends Seq<R, U>> U mapAbort(
        @NotNull Result.AbortableFunction<? super T, ? extends R> function) throws AbortOnFailure {
        var escapeHatch = new ThrowableEscapeHatch<>(AbortOnFailure.class);
        var res = (U) Seq.<R, U>of(
            new MapIterator<>(this.iterator, ThrowableEscapeHatch.escape(function, escapeHatch)));
        escapeHatch.rethrow();
        return res;
    }

    @SuppressWarnings("unchecked")
    public @NotNull <R, U extends Seq<Result<R>, U>> U mapThrowing(
        @NotNull ThrowingFunction<? super T, ? extends R> function) {
        return (U) Seq.<Result<R>, U>of(new MapIterator<>(this.iterator, arg -> {
            try {
                var ret = function.apply(arg);
                return (Success<R>) () -> ret;
            } catch (Throwable throwable) {
                return (Failure<R>) () -> throwable;
            }
        }));
    }

    @SuppressWarnings("unchecked")
    public @NotNull <R, U extends Seq<R, U>> U mapNotNull(
        @NotNull Function<? super T, ? extends R> function) {
        return (U) Seq.<R, U>of(
            new FilterIterator<>(new MapIterator<>(this.iterator, function), Objects::nonNull));
    }

    @SuppressWarnings("unchecked")
    public @NotNull <R extends Number, U extends NumberSeq<R>> U mapToNumber(
        @NotNull Function<? super T, ? extends R> function) {
        return (U) ofNumeric(new MapIterator<>(this.iterator, function));
    }

    @SuppressWarnings("unchecked")
    public @NotNull <R, U extends Seq<R, U>> U flatMap(
        @NotNull Function<? super T, ? extends Seq<? extends R, ?>> function) {
        return (U) Seq.<R, U>of(new FlatMapIterator<>(this.iterator, function));
    }

    public @NotNull T reduce(@NotNull T identity, @NotNull BinaryOperator<T> accumulator) {
        var current = identity;
        if (this.iterator.hasNext()) {
            while (this.iterator.hasNext()) {
                current = accumulator.apply(current, this.iterator.next());
            }
        }
        return current;
    }

    public @NotNull Optional<T> reduce(@NotNull BinaryOperator<T> accumulator) {
        if (!this.iterator.hasNext()) {
            return Optional.empty();
        } else {
            var current = this.iterator.next();
            if (this.iterator.hasNext()) {
                while (this.iterator.hasNext()) {
                    current = accumulator.apply(current, this.iterator.next());
                }
            }
            return Optional.ofNullable(current);
        }
    }

    @SuppressWarnings("unchecked")
    public @NotNull S peek(@NotNull Consumer<T> consumer) {
        this.iterator = new PeekIterator<>(this.iterator, consumer);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull S distinct() {
        this.iterator = new DistinctIterator<>(this.iterator);
        return (S) this;
    }

    public @NotNull S sorted() {
        return this.sorted(new GenericComparator<>());
    }

    @SuppressWarnings("unchecked")
    public @NotNull S sorted(@NotNull Comparator<? super T> comparator) {
        var list = this.toList();
        list.sort(comparator);
        return (S) of(list);
    }

    public @NotNull S sortedDescending() {
        return this.sortedDescending(new GenericComparator<>().reversed());
    }

    @SuppressWarnings("unchecked")
    public @NotNull S sortedDescending(@NotNull Comparator<? super T> comparator) {
        var list = this.toList();
        list.sort(comparator.reversed());
        return (S) of(list);
    }

    @SuppressWarnings("unchecked")
    public @NotNull S reversed() {
        var list = this.toList();
        Collections.reverse(list);
        return (S) of(list);
    }

    @SuppressWarnings("unchecked")
    public @NotNull S limit(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Value of maxSize must be greater then 0.");
        }
        this.iterator = new LimitIterator<>(this.iterator, maxSize);
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull S skip(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Value of maxSize must be greater then 0.");
        }
        this.iterator = new SkipIterator<>(this.iterator, maxSize);
        return (S) this;
    }

    public void forEach(@NotNull Consumer<? super T> consumer) {
        while (this.iterator.hasNext()) {
            consumer.accept(this.iterator.next());
        }
    }

    public void forEach(@NotNull BiConsumer<? super T, Integer> consumer) {
        var i = 0;
        while (this.iterator.hasNext()) {
            consumer.accept(this.iterator.next(), i);
            i++;
        }
    }

    public @NotNull <R, A> R collect(@NotNull Collector<? super T, A, R> collector) {
        var resultContainer = collector.supplier().get();
        var accumulator = collector.accumulator();
        while (this.iterator.hasNext()) {
            accumulator.accept(resultContainer, this.iterator.next());
        }
        return collector.finisher().apply(resultContainer);
    }

    @SuppressWarnings("unchecked")
    public @NotNull S concat(@NotNull Seq<T, S>... sequences) {
        this.iterator = new ConcatIterator<>(this.iterator,
            Seq.of(sequences).map(seq -> seq.iterator).toList());
        return (S) this;
    }

    @SuppressWarnings("unchecked")
    public @NotNull S merge(@NotNull Seq<T, S>... sequences) {
        this.iterator = new MergeIterator<>(this.iterator,
            Seq.of(sequences).map(seq -> seq.iterator).toList());
        return (S) this;
    }

    public @NotNull List<T> toList() {
        return this.collect(Collectors.toList());
    }

    public @NotNull List<T> toUnmodifiableList() {
        return this.collect(Collectors.toUnmodifiableList());
    }

    public @NotNull Set<T> toSet() {
        return this.collect(Collectors.toSet());
    }

    public @NotNull Set<T> toUnmodifiableSet() {
        return this.collect(Collectors.toUnmodifiableSet());
    }

    public @NotNull <K> Map<? extends K, List<T>> groupBy(
        @NotNull Function<? super T, ? extends K> classifier) {
        return this.collect(Collectors.groupingBy(classifier));
    }

    public @NotNull <K, A, D> Map<? extends K, D> groupBy(
        @NotNull Function<? super T, ? extends K> classifier,
        @NotNull Collector<? super T, A, D> downstream) {
        return this.collect(Collectors.groupingBy(classifier, downstream));
    }

    public @NotNull <K, A, D, M extends Map<K, D>> Map<? extends K, D> groupBy(
        @NotNull Function<? super T, ? extends K> classifier, @NotNull Supplier<M> mapFactory,
        @NotNull Collector<? super T, A, D> downstream) {
        return this.collect(Collectors.groupingBy(classifier, mapFactory, downstream));
    }

    public @NotNull String joining() {
        return this.map(T::toString).collect(Collectors.joining());
    }

    public @NotNull String joining(@NotNull CharSequence delimiter) {
        return this.map(T::toString).collect(Collectors.joining(delimiter));
    }

    public @NotNull Optional<T> min(@NotNull Comparator<? super T> comparator) {
        if (!this.iterator.hasNext()) {
            return Optional.empty();
        }
        var min = this.iterator.next();
        while (this.iterator.hasNext()) {
            var element = this.iterator.next();
            if (comparator.compare(element, min) < 0) {
                min = element;
            }
        }
        return Optional.ofNullable(min);
    }

    public @NotNull Optional<T> max(@NotNull Comparator<? super T> comparator) {
        if (!this.iterator.hasNext()) {
            return Optional.empty();
        }
        var max = this.iterator.next();
        while (this.iterator.hasNext()) {
            var element = this.iterator.next();
            if (comparator.compare(max, element) > 0) {
                max = element;
            }
        }
        return Optional.ofNullable(max);
    }

    public @NotNull Optional<T> findFirst() {
        return !this.iterator.hasNext() ? Optional.empty()
            : Optional.ofNullable(this.iterator.next());
    }

    public @NotNull Optional<T> findAny() {
        if (!this.iterator.hasNext()) {
            return Optional.empty();
        }
        T last = this.iterator.next();
        while (last == null && this.iterator.hasNext()) {
            last = this.iterator.next();
        }
        return Optional.ofNullable(last);
    }

    public @NotNull Optional<T> findLast() {
        if (!this.iterator.hasNext()) {
            return Optional.empty();
        }
        T last = this.iterator.next();
        while (this.iterator.hasNext()) {
            last = this.iterator.next();
        }
        return Optional.ofNullable(last);
    }

    public int count() {
        var count = 0;
        while (this.iterator.hasNext()) {
            count++;
            this.iterator.next();
        }
        return count;
    }

    public boolean anyMatch(@NotNull Predicate<? super T> predicate) {
        var iterator = new MapIterator<>(this.iterator, predicate::test);
        while (iterator.hasNext()) {
            if (iterator.next()) {
                return true;
            }
        }
        return false;
    }

    public boolean allMatch(@NotNull Predicate<? super T> predicate) {
        return !this.anyMatch(predicate.negate());
    }

    public boolean noneMatch(@NotNull Predicate<? super T> predicate) {
        return !this.allMatch(predicate);
    }

    private static final class GenericComparator<T> implements Comparator<T> {

        @Override
        @Contract(pure = true)
        @SuppressWarnings("unchecked")
        public int compare(T a, T b) {
            if (a instanceof Comparable) {
                return ((Comparable<T>) a).compareTo(b);
            }
            return 0;
        }

    }

}
