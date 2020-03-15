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

package grevend.persistence.lite.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TupleTest {

  private Tuple tuple = Tuple.of(12, "test", true, 42f, 128d);

  @Test
  void testTupleSize() {
    assertThat(this.tuple.count()).isEqualTo(5);
    assertThat(this.tuple.getElements().size()).isEqualTo(5);
  }

  @Test
  void testTupleGet() {
    int i = this.tuple.get(0, Integer.TYPE);
    String text = this.tuple.get(1, String.class);
    boolean b = this.tuple.get(2, Boolean.TYPE);
  }

  @Test
  void testTupleToString() {
    assertThat(this.tuple.toString()).isEqualTo("Tuple{elements=[12, test, true, 42.0, 128.0]}");
  }

}
