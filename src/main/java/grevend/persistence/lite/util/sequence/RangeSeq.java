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
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class RangeSeq<T, S> implements Seq<T> {

  private T start, end;
  private TriFunction<T, T, S, T> stepper;
  private S step;

  public RangeSeq(@NotNull T start, @NotNull T end, @NotNull TriFunction<T, T, S, T> stepper,
      S step) {
    this.start = start;
    this.end = end;
    this.stepper = stepper;
    this.step = step;
  }

  @Override
  public @NotNull Iterator<T> iterator() {
    var start = this.start;
    var end = this.end;
    var step = this.step;
    var stepper = this.stepper;
    return new Iterator<>() {

      private T current;

      @Override
      public boolean hasNext() {
        return !end.equals(this.current);
      }

      @Override
      public T next() {
        if (this.current == null) {
          return (this.current = start);
        }
        if (this.hasNext()) {
          this.current = stepper.apply(this.current, end, step);
        }
        return this.current;
      }

    };
  }

}
