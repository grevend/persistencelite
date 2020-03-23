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

import java.util.Iterator;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public class FilterIter<T> extends ChainIter<T> {

  private final Predicate<? super T> predicate;
  private T next;
  private boolean isNextSet = false;

  public FilterIter(@NotNull Iterator<T> iterator, @NotNull Predicate<? super T> predicate) {
    super(iterator);
    this.predicate = predicate;
  }

  @Override
  public boolean hasNext() {
    return this.isNextSet || this.setNext();
  }

  private boolean setNext() {
    while (this.iterator.hasNext()) {
      var element = this.iterator.next();
      if (this.predicate.test(element)) {
        this.next = element;
        this.isNextSet = true;
        return true;
      }
    }
    return false;
  }

  @Override
  public T next() {
    this.isNextSet = false;
    return this.next;
  }

}
