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
import java.util.Queue;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class FlatMapSeq<T, R> implements Seq<R> {

  private final Seq<T> seq;
  private final Function<? super T, ? extends Seq<? extends R>> function;

  public FlatMapSeq(@NotNull Seq<T> seq,
      @NotNull Function<? super T, ? extends Seq<? extends R>> function) {
    this.seq = seq;
    this.function = function;
  }

  @Override
  public @NotNull Iterator<R> iterator() {
    var iterator = this.seq.iterator();
    var function = this.function;
    Queue<Iterator<? extends R>> queue = new ArrayDeque<>();
    if (iterator.hasNext()) {
      queue.offer(function.apply(iterator.next()).iterator());
    }
    return new Iterator<>() {

      @Override
      public boolean hasNext() {
        while (!queue.isEmpty()) {
          if (queue.peek().hasNext()) {
            return true;
          } else {
            if (iterator.hasNext()) {
              queue.offer(function.apply(iterator.next()).iterator());
            }
            queue.poll();
          }
        }
        return false;
      }

      @Override
      public R next() {
        return queue.element().next();
      }

    };
  }
}
