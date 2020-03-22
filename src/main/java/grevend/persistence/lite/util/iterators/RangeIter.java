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

package grevend.persistence.lite.util.iterators;

import grevend.persistence.lite.util.TriFunction;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class RangeIter<T, S> implements Iterator<T> {

  private final T start, end;
  private final TriFunction<T, T, S, T> stepper;
  private final S step;

  private T current;

  public RangeIter(@NotNull T start, @NotNull T end, @NotNull TriFunction<T, T, S, T> stepper,
      @NotNull S step) {
    this.start = start;
    this.end = end;
    this.stepper = stepper;
    this.step = step;
  }

  @Override
  public boolean hasNext() {
    return !this.end.equals(this.current);
  }

  @Override
  public T next() {
    if (this.current == null) {
      return (this.current = this.start);
    }
    if (this.hasNext()) {
      this.current = this.stepper.apply(this.current, this.end, this.step);
    }
    return this.current;
  }

}
