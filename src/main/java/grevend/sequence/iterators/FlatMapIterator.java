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

package grevend.sequence.iterators;

import grevend.sequence.Seq;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class FlatMapIterator<T, R, U extends Seq<R, ?>> implements Iterator<R> {

    private final Iterator<T> iterator;
    private final Function<? super T, ? extends Seq<? extends R, ?>> function;
    private final Queue<Iterator<? extends R>> queue;

    public FlatMapIterator(@NotNull Iterator<T> iterator, @NotNull Function<? super T, ? extends Seq<? extends R, ?>> function) {
        this.iterator = iterator;
        this.function = function;
        this.queue = new ArrayDeque<>();
        if (iterator.hasNext()) {
            this.queue.offer(function.apply(iterator.next()).iterator());
        }
    }

    @Override
    public boolean hasNext() {
        while (!this.queue.isEmpty()) {
            if (this.queue.peek().hasNext()) {
                return true;
            } else {
                if (this.iterator.hasNext()) {
                    this.queue.offer(this.function.apply(this.iterator.next()).iterator());
                }
                this.queue.poll();
            }
        }
        return false;
    }

    @Override
    public R next() {
        return this.queue.element().next();
    }

}
