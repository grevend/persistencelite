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
import org.jetbrains.annotations.NotNull;

public class LimitSeq<T> implements Seq<T> {

  private Seq<T> seq;
  private int maxSize;

  public LimitSeq(@NotNull Seq<T> seq, int maxSize) {
    this.seq = seq;
    this.maxSize = maxSize;
  }

  @Override
  public @NotNull Iterator<T> iterator() {
    var iterator = this.seq.iterator();
    var maxSize = this.maxSize;
    return new Iterator<>() {

      private int i = 0;

      @Override
      public boolean hasNext() {
        return this.i < maxSize && iterator.hasNext();
      }

      @Override
      public T next() {
        this.i++;
        return iterator.next();
      }

    };
  }

}
