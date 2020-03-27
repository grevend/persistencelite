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

package grevend.persistencelite.util;

import grevend.jacoco.Generated;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Tuple {

  private final List<Object> elements;

  private Tuple(@NotNull Collection<Object> elements) {
    this.elements = new ArrayList<>();
    this.elements.addAll(elements);
  }

  @Contract("_ -> new")
  public static @NotNull Tuple of(@NotNull Collection<Object> elements) {
    return new Tuple(elements);
  }

  @Contract("_ -> new")
  public static @NotNull Tuple of(Object... elements) {
    return of(List.of(elements));
  }

  @SuppressWarnings("unchecked")
  public <A> A get(int index, @NotNull Class<A> clazz) {
    return (A) this.elements.get(index);
  }

  public int count() {
    return this.elements.size();
  }

  public @NotNull List<Object> getElements() {
    return this.elements;
  }

  @Override
  @Generated
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    Tuple tuple = (Tuple) o;
    return Objects.equals(this.getElements(), tuple.getElements());
  }

  @Override
  @Generated
  public int hashCode() {
    return Objects.hash(this.getElements());
  }

  @Override
  public String toString() {
    return "Tuple{" +
        "elements=" + this.elements +
        '}';
  }

}
