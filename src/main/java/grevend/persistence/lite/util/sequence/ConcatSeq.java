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

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;

public class ConcatSeq<T> implements Seq<T> {

  private final Seq<T> a;
  private final Seq<? extends T> b;

  public ConcatSeq(@NotNull Seq<T> a, @NotNull Seq<? extends T> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public @NotNull Iterator<T> iterator() {
    Queue<Iterator<? extends T>> queue = new ArrayDeque<>(
        List.of(this.a.iterator(), this.b.iterator()));
    return new Iterator<>() {

      @Override
      public boolean hasNext() {
        while (!queue.isEmpty()) {
          if (queue.peek().hasNext()) {
            return true;
          }
          queue.poll();
        }
        return false;
      }

      @Override
      public T next() {
        if (!this.hasNext()) {
          throw new NoSuchElementException();
        }
        var iterator = queue.poll();
        if (iterator == null) {
          throw new IllegalStateException();
        }
        var res = iterator.next();
        queue.offer(iterator);
        return res;
      }

    };
  }
}
