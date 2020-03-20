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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public interface Seq<T> {

  static @NotNull Seq<?> empty() {
    return () -> new Iterator<>() {

      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public Object next() {
        return null;
      }

    };
  }

  static @NotNull <T> Seq<T> of(@NotNull Iterator<T> iterator) {
    return () -> iterator;
  }

  static @NotNull <T> Seq<T> of(@NotNull Iterable<T> iterable) {
    return of(iterable.iterator());
  }

  static @NotNull <T> Seq<T> concat(@NotNull Seq<T> a, @NotNull Seq<T> b) {
    return a.concat(b);
  }

  static @NotNull <T> Seq<T> generate(@NotNull Supplier<T> supplier) {
    return new GeneratorSeq<T>(supplier);
  }

  @NotNull Iterator<T> iterator();

  default @NotNull Seq<T> filter(@NotNull Predicate<? super T> predicate) {
    return new FilterSeq<>(this, predicate);
  }

  default @NotNull <R> Seq<R> map(@NotNull Function<? super T, ? extends R> function) {
    return new MapSeq<>(this, function);
  }

  default @NotNull <R> Seq<R> flatMap(
      @NotNull Function<? super T, ? extends Seq<? extends R>> function) {
    return null;
  }

  default @NotNull Seq<T> distinct() {
    return null;
  }

  default @NotNull Seq<T> sorted() {
    return null;
  }

  default @NotNull Seq<T> sorted(@NotNull Comparator<? super T> comparator) {
    return null;
  }

  default @NotNull Seq<T> reversed() {
    return null;
  }

  default @NotNull Seq<T> reversed(@NotNull Comparator<? super T> comparator) {
    return null;
  }

  default @NotNull Seq<T> limit(int i) {
    return null;
  }

  default @NotNull Seq<T> skip(int i) {
    return null;
  }

  default void forEach(@NotNull Consumer<? super T> consumer) {
    var iterator = this.iterator();
    while (iterator.hasNext()) {
      consumer.accept(iterator.next());
    }
  }

  default void forEach(@NotNull BiConsumer<? super T, Integer> consumer) {
    var iterator = this.iterator();
    var i = 0;
    while (iterator.hasNext()) {
      consumer.accept(iterator.next(), i);
      i++;
    }
  }

  default @NotNull <R, A> R collect(@NotNull Collector<? super T, A, R> collector) {
    var iterator = this.iterator();
    var resultContainer = collector.supplier().get();
    var accumulator = collector.accumulator();
    while (iterator.hasNext()) {
      accumulator.accept(resultContainer, iterator.next());
    }
    return collector.finisher().apply(resultContainer);
  }

  default @NotNull Seq<T> concat(@NotNull Seq<? extends T> seq) {
    return new ConcatSeq<>(this, seq);
  }

  default @NotNull List<T> toList() {
    return this.collect(Collectors.toList());
  }

  default @NotNull List<T> toUnmodifiableList() {
    return this.collect(Collectors.toUnmodifiableList());
  }

  default @NotNull Set<T> toSet() {
    return this.collect(Collectors.toSet());
  }

  default @NotNull Set<T> toUnmodifiableSet() {
    return this.collect(Collectors.toUnmodifiableSet());
  }

  default @NotNull Optional<T> min(@NotNull Comparator<? super T> comparator) {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    var max = iterator.next();
    while (iterator.hasNext()) {
      var element = iterator.next();
      if (comparator.compare(max, element) < 0) {
        max = element;
      }
    }
    return Optional.ofNullable(max);
  }

  default @NotNull Optional<T> max(@NotNull Comparator<? super T> comparator) {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    var max = iterator.next();
    while (iterator.hasNext()) {
      var element = iterator.next();
      if (comparator.compare(max, element) > 0) {
        max = element;
      }
    }
    return Optional.ofNullable(max);
  }

  default @NotNull Optional<T> findFirst() {
    var iterator = this.iterator();
    return !iterator.hasNext() ? Optional.empty() : Optional.ofNullable(iterator.next());
  }

  default @NotNull Optional<T> findAny() {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    T last = iterator.next();
    while (last == null && iterator.hasNext()) {
      last = iterator.next();
    }
    return Optional.ofNullable(last);
  }

  default @NotNull Optional<T> findLast() {
    var iterator = this.iterator();
    if (!iterator.hasNext()) {
      return Optional.empty();
    }
    T last = iterator.next();
    while (iterator.hasNext()) {
      last = iterator.next();
    }
    return Optional.ofNullable(last);
  }

  default int count() {
    var count = 0;
    while (this.iterator().hasNext()) {
      count++;
      this.iterator().next();
    }
    return count;
  }

  default boolean anyMatch(@NotNull Predicate<? super T> predicate) {
    return false;
  }

  default boolean allMatch(@NotNull Predicate<? super T> predicate) {
    return false;
  }

  default boolean noneMatch(@NotNull Predicate<? super T> predicate) {
    return false;
  }

}
