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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeqTest {

  private List<Integer> integers;
  private Seq<Integer> integerSeq;
  private Seq<?> emptySeq;

  @BeforeEach
  void initIntegers() {
    this.integers = List.of(12, 42, 24, 7, 9, 89, 112);
    this.integerSeq = Seq.of(this.integers);
    this.emptySeq = Seq.empty();
  }

  @Test
  void testSeqCount() {
    assertThat(this.integerSeq.count()).isEqualTo(this.integers.size());
    assertThat(this.emptySeq.count()).isEqualTo(0);
  }

  @Test
  void testSeqFindFirst() {
    var res = this.integerSeq.findFirst();
    assertThat(res).isPresent();
    assertThat(res).get().isEqualTo(12);
  }

  @Test
  void testSeqFindFirstWithEmptySeq() {
    var res = this.emptySeq.findFirst();
    assertThat(res).isEmpty();
  }

  @Test
  void testSeqFindLast() {
    var res = this.integerSeq.findLast();
    assertThat(res).isPresent();
    assertThat(res).get().isEqualTo(112);
  }

  @Test
  void testSeqFindLastWithEmptySeq() {
    var res = this.emptySeq.findLast();
    assertThat(res).isEmpty();
  }

  @Test
  void testSeqForEach() {
    var builder1 = new StringBuilder();
    var builder2 = new StringBuilder();
    var builder3 = new StringBuilder();

    this.integers.forEach(builder1::append);
    this.integers.stream().forEach(builder2::append);
    this.integerSeq.forEach(builder3::append);

    assertThat(builder3.toString()).isEqualTo(builder1.toString());
    assertThat(builder3.toString()).isEqualTo(builder2.toString());
  }

  @Test
  void testSeqForEachWithIndex() {
    var builder = new StringBuilder();

    this.integerSeq.forEach(
        (element, index) -> builder.append(index).append(" : ").append(element).append(", "));

    assertThat(builder.toString())
        .isEqualTo("0 : 12, 1 : 42, 2 : 24, 3 : 7, 4 : 9, 5 : 89, 6 : 112, ");
  }

  @Test
  void testSeqCollectToList() {
    var res = this.integerSeq.toList();
    assertThat(res).containsExactlyElementsOf(this.integers);
    res.add(55);
  }

  @Test
  void testSeqCollectToUnmodifiableList() {
    var res = this.integerSeq.toUnmodifiableList();
    assertThat(res).containsExactlyElementsOf(this.integers);
    assertThatThrownBy(() -> res.add(55)).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void testSeqCollectToSet() {
    var res = this.integerSeq.toSet();
    assertThat(res).containsExactlyInAnyOrderElementsOf(this.integers);
    res.add(55);
  }

  @Test
  void testSeqCollectToUnmodifiableSet() {
    var res = this.integerSeq.toUnmodifiableSet();
    assertThat(res).containsExactlyInAnyOrderElementsOf(this.integers);
    assertThatThrownBy(() -> res.add(55)).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

}
