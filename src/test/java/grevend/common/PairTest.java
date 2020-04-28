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

package grevend.common;

import static org.assertj.core.api.Assertions.assertThat;

import grevend.common.Pair;
import java.util.List;
import org.junit.jupiter.api.Test;

class PairTest {

    @Test
    void testPairOf() {
        var pair = Pair.of("test", 45L);
        assertThat(pair.getA()).isEqualTo("test");
        assertThat(pair.getB()).isEqualTo(45L);
    }

    @Test
    void testPairToString() {
        var pair = Pair.of("test", 45L);
        assertThat(pair.toString()).isEqualTo("Pair{a=test, b=45}");
    }

    @Test
    void testPairWithNullToString() {
        var pair = Pair.of(null, null);
        assertThat(pair.toString()).isEqualTo("Pair{a=null, b=null}");
    }

    /*@Test
    void testPairWithArraysToString() {
        var pair = Pair.of(new Option<?>[]{Option.of(12)}, new Option<?>[]{Option.of(21)});
        assertThat(pair.toString()).isEqualTo("Pair{a=[Option[12]], b=[Option[21]]}");
    }*/

    @Test
    void testPairWithPrimitiveArraysToString() {
        var pair = Pair.of(new int[]{12, 42}, new long[]{21, 24});
        assertThat(pair.toString()).contains("Pair{a=[I@");
        assertThat(pair.toString()).contains(", b=[J@");
    }

    @Test
    void testPairToMap() {
        var map = List.of(Pair.of(12, 21), Pair.of(34, 43), Pair.of(56, 65)).stream()
            .collect(Pair.toMap());
        assertThat(map).containsKeys(12, 34, 56);
        assertThat(map).containsValues(21, 43, 65);
    }

}
