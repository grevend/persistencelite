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

package grevend.persistence.lite.util.sequence;

import grevend.persistence.lite.util.TriFunction;
import grevend.persistence.lite.util.Utils;
import grevend.persistence.lite.util.iterators.ConcatIter;
import grevend.persistence.lite.util.iterators.DistinctIter;
import grevend.persistence.lite.util.iterators.FilterIter;
import grevend.persistence.lite.util.iterators.FlatMapIter;
import grevend.persistence.lite.util.iterators.GeneratorIter;
import grevend.persistence.lite.util.iterators.LimitIter;
import grevend.persistence.lite.util.iterators.MapIter;
import grevend.persistence.lite.util.iterators.MergeIter;
import grevend.persistence.lite.util.iterators.PeekIter;
import grevend.persistence.lite.util.iterators.RangeIter;
import grevend.persistence.lite.util.iterators.SkipIter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class Seq<T, S extends Seq<T, S>> {

  protected Iterator<T> iterator;

  Seq(@NotNull Iterator<T> iterator) {
    this.iterator = iterator;
  }

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
  }

  public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(@NotNull Iterator<T> iterator) {
    return new Seq<>(iterator);
  }

  public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(@NotNull Iterable<T> iterable) {
    return of(iterable.iterator());
  }

  @SafeVarargs
  public static @NotNull <T, S extends Seq<T, S>> Seq<T, S> of(T... values) {
    return of(Arrays.asList(values));
  }

  @SafeVarargs
  public static @NotNull <T extends Number> NumberSeq<T> of(T... values) {
    return new NumberSeq<>(Arrays.asList(values).iterator());
  }

  public static @NotNull <T, S extends Seq<T, S>, S2> Seq<T, S> range(@NotNull T startInclusive,
      @NotNull T endInclusive, @NotNull TriFunction<T, T, S2, T> stepper, @NotNull S2 stepSize) {
    return of(new RangeIter<>(startInclusive, endInclusive, stepper, stepSize));
  }

  public static @NotNull <T extends Number> NumberSeq<T> range(@NotNull T startInclusive,
      @NotNull T endInclusive, @NotNull TriFunction<T, T, T, T> stepper, @NotNull T stepSize) {
    return new NumberSeq<>(new RangeIter<>(startInclusive, endInclusive, stepper, stepSize));
  }

  public static @NotNull <T extends Number> NumberSeq<T> range(@NotNull T startInclusive,
      @NotNull T endInclusive, @NotNull T stepSize) {
    return new NumberSeq<>(new RangeIter<>(startInclusive, endInclusive,
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
    return (S) of(new GeneratorIter<>(supplier));
  }

  public @NotNull Iterator<T> iterator() {
    return this.iterator;
  }

  @SuppressWarnings("unchecked")
  public @NotNull S filter(@NotNull Predicate<? super T> predicate) {
    this.iterator = new FilterIter<>(this.iterator, predicate);
    return (S) this;
  }

  @SuppressWarnings("unchecked")
  public @NotNull <R, U extends Seq<R, U>> U map(
      @NotNull Function<? super T, ? extends R> function) {
    return (U) Seq.<R, U>of(new MapIter<>(this.iterator, function));
  }

  @SuppressWarnings("unchecked")
  public @NotNull <R, U extends Seq<R, U>> U flatMap(
      @NotNull Function<? super T, ? extends Seq<? extends R, ?>> function) {
    return (U) Seq.<R, U>of(new FlatMapIter<>(this.iterator, function));
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
    this.iterator = new PeekIter<>(this.iterator, consumer);
    return (S) this;
  }

  @SuppressWarnings("unchecked")
  public @NotNull S distinct() {
    this.iterator = new DistinctIter<>(this.iterator);
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
    this.iterator = new LimitIter<>(this.iterator, maxSize);
    return (S) this;
  }

  @SuppressWarnings("unchecked")
  public @NotNull S skip(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("Value of maxSize must be greater then 0.");
    }
    this.iterator = new SkipIter<>(this.iterator, maxSize);
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
    this.iterator = new ConcatIter<>(this.iterator,
        Seq.of(sequences).map(seq -> seq.iterator).toList());
    return (S) this;
  }

  @SuppressWarnings("unchecked")
  public @NotNull S merge(@NotNull Seq<T, S>... sequences) {
    this.iterator = new MergeIter<>(this.iterator,
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
    return !this.iterator.hasNext() ? Optional.empty() : Optional.ofNullable(this.iterator.next());
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
    var iterator = new MapIter<>(this.iterator, predicate::test);
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
    @SuppressWarnings("unchecked")
    public int compare(T a, T b) {
      if (a instanceof Comparable) {
        return ((Comparable<T>) a).compareTo(b);
      }
      return 0;
    }

  }

}
