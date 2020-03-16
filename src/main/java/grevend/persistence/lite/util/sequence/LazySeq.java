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
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import org.jetbrains.annotations.NotNull;

public abstract class LazySeq<T> implements Seq<T> {

  protected final LazySeq<T> seq;

  public LazySeq(LazySeq<T> seq) {
    this.seq = seq;
  }

  public static @NotNull <T> LazySeq<T> of(@NotNull final Iterator<T> iterator) {
    return new LazySeq<T>(null) {

      @NotNull
      @Override
      public Iterator<T> iterator() {
        return iterator;
      }

    };
  }

  public static @NotNull <T> Seq<T> of(@NotNull final Iterable<T> iterable) {
    return of(iterable.iterator());
  }

  public abstract @NotNull Iterator<T> iterator();

  public @NotNull Seq<T> filter(@NotNull Predicate<? super T> predicate) {
    return new FilterSeq<>(this, predicate);
  }

  public @NotNull <R> Seq<R> map(@NotNull Function<? super T, ? extends R> function) {
    return null;
  }

  public @NotNull <R> Seq<R> flatMap(
      @NotNull Function<? super T, ? extends Seq<? extends R>> function) {
    return null;
  }

  public @NotNull Seq<T> distinct() {
    return null;
  }

  public @NotNull Seq<T> sort(@NotNull Comparator<? super T> comparator) {
    return null;
  }

  public @NotNull Seq<T> limit(int i) {
    return null;
  }

  public @NotNull Seq<T> skip(int i) {
    return null;
  }

  public void forEach(@NotNull Consumer<? super T> consumer) {
    var iterator = this.iterator();
    while (iterator.hasNext()) {
      consumer.accept(iterator.next());
    }
  }

  public void forEach(@NotNull BiConsumer<? super T, Integer> consumer) {
    var iterator = this.iterator();
    var i = 0;
    while (iterator.hasNext()) {
      consumer.accept(iterator.next(), i);
      i++;
    }
  }

  public @NotNull <R, A> R collect(@NotNull Collector<? super T, A, R> collector) {
    return null;
  }

  public @NotNull Optional<T> min(@NotNull Comparator<? super T> comparator) {
    return Optional.empty();
  }

  public @NotNull Optional<T> max(@NotNull Comparator<? super T> comparator) {
    return Optional.empty();
  }

  public int count() {
    return 0;
  }

  public boolean anyMatch(@NotNull Predicate<? super T> predicate) {
    return false;
  }

  public boolean allMatch(@NotNull Predicate<? super T> predicate) {
    return false;
  }

  public boolean noneMatch(@NotNull Predicate<? super T> predicate) {
    return false;
  }

}
