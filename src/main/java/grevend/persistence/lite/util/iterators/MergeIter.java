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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;

public class MergeIter<T> extends ChainIter<T> {

  private final Queue<Iterator<? extends T>> queue;

  public MergeIter(@NotNull Iterator<T> iterator, @NotNull Collection<Iterator<T>> iterators) {
    super(iterator);
    this.queue = new ArrayDeque<>();
    this.queue.add(iterator);
    this.queue.addAll(iterators);
  }

  @Override
  public boolean hasNext() {
    while (!this.queue.isEmpty()) {
      if (this.queue.peek().hasNext()) {
        return true;
      }
      this.queue.poll();
    }
    return false;
  }

  @Override
  public T next() {
    T element = null;
    var iterator = this.queue.poll();
    if (iterator != null) {
      element = iterator.next();
      this.queue.offer(iterator);
    }
    return element;
  }

}
