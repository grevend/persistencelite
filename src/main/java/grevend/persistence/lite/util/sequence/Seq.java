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

public interface Seq<T> {

  @NotNull Iterator<T> iterator();

  @NotNull Seq<T> filter(@NotNull Predicate<? super T> predicate);

  @NotNull <R> Seq<R> map(@NotNull Function<? super T, ? extends R> function);

  @NotNull <R> Seq<R> flatMap(@NotNull Function<? super T, ? extends Seq<? extends R>> function);

  @NotNull Seq<T> distinct();

  @NotNull Seq<T> sort(@NotNull Comparator<? super T> comparator);

  @NotNull Seq<T> limit(int i);

  @NotNull Seq<T> skip(int i);

  void forEach(@NotNull Consumer<? super T> consumer);

  void forEach(@NotNull BiConsumer<? super T, Integer> consumer);

  @NotNull <R, A> R collect(@NotNull Collector<? super T, A, R> collector);

  @NotNull Optional<T> min(@NotNull Comparator<? super T> comparator);

  @NotNull Optional<T> max(@NotNull Comparator<? super T> comparator);

  int count();

  boolean anyMatch(@NotNull Predicate<? super T> predicate);

  boolean allMatch(@NotNull Predicate<? super T> predicate);

  boolean noneMatch(@NotNull Predicate<? super T> predicate);

}
