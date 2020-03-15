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

class TripletTest {

  @Test
  void testTripletOf() {
    var triplet = Triplet.of("test", 45L, true);
    assertThat(triplet.getA()).isEqualTo("test");
    assertThat(triplet.getB()).isEqualTo(45L);
    assertThat(triplet.getC()).isEqualTo(true);
  }

  @Test
  void testTripletToString() {
    var triplet = Triplet.of("test", 45L, true);
    assertThat(triplet.toString()).isEqualTo("Triplet{a=test, b=45, c=true}");
  }

  @Test
  void testTripletWithNullToString() {
    var triplet = Triplet.of(null, null, null);
    assertThat(triplet.toString()).isEqualTo("Triplet{a=null, b=null, c=null}");
  }

  @Test
  void testTripletWithArraysToString() {
    var triplet = Triplet.of(new Option<?>[]{Option.of(12)}, new Option<?>[]{Option.of(21)},
        new Option<?>[]{Option.of(21)});
    assertThat(triplet.toString())
        .isEqualTo("Triplet{a=[Option[12]], b=[Option[21]], c=[Option[21]]}");
  }

  @Test
  void testTripletWithPrimitiveArraysToString() {
    var triplet = Triplet.of(new int[]{12, 42}, new long[]{21, 24}, new byte[]{12, 24});
    assertThat(triplet.toString()).contains("Triplet{a=[I@");
    assertThat(triplet.toString()).contains(", b=[J@");
    assertThat(triplet.toString()).contains(", c=[B@");
  }

}
