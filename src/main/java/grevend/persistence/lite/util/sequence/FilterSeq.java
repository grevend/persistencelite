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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public class FilterSeq<T> extends LazySeq<T> {

  private Predicate<? super T> predicate;

  public FilterSeq(LazySeq<T> seq, Predicate<? super T> predicate) {
    super(seq);
    this.predicate = predicate;
  }

  @Override
  public @NotNull Iterator<T> iterator() {
    var iterator = this.seq.iterator();
    var predicate = this.predicate;
    return new Iterator<>() {

      private T next;
      private boolean isNextSet = false;

      @Override
      public boolean hasNext() {
        return this.isNextSet || this.setNext();
      }

      private boolean setNext() {
        while (iterator.hasNext()) {
          var obj = iterator.next();
          if (predicate.test(obj)) {
            this.next = obj;
            this.isNextSet = true;
            return true;
          }
        }
        return false;
      }

      @Override
      public T next() {
        if (!this.isNextSet && !this.setNext()) {
          throw new NoSuchElementException();
        }
        this.isNextSet = false;
        return this.next;
      }

    };
  }

}
