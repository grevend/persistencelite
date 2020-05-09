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

package grevend.sequence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeqTest<SI extends Seq<Integer, SI>> {

    private List<Integer> integers;
    private Seq<Integer, SI> integerSeq;
    private Seq<Integer, SI> emptySeq;

    @BeforeEach
    void initIntegers() {
        this.integers = List.of(12, 42, 24, 7, 9, 89, 112);
        this.integerSeq = Seq.of(this.integers);
        this.emptySeq = Seq.empty();
    }

    @Test
    void testEmptySeq() {
        assertThat(this.emptySeq.iterator().hasNext()).isFalse();
        assertThat(this.emptySeq.iterator().next()).isNull();
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
    void testSeqFindAny() {
        var res = Seq.of(null, null, 12, 43).findAny();
        assertThat(res).isPresent();
        assertThat(res).get().isEqualTo(12);
    }

    @Test
    void testSeqFindAnyWithEmptySeq() {
        var res = this.emptySeq.findAny();
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
        assertThatThrownBy(() -> res.add(55))
            .isExactlyInstanceOf(UnsupportedOperationException.class);
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
        assertThatThrownBy(() -> res.add(55))
            .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testSeqConcat() {
        var res = this.integerSeq.concat(Seq.of(List.of(64, 44))).toList();
        assertThat(res).containsExactly(12, 42, 24, 7, 9, 89, 112, 64, 44);
    }

    @Test
    void testSeqMerge() {
        var res = this.integerSeq.merge(Seq.of(List.of(64, 44))).toList();
        assertThat(res).containsExactly(12, 64, 42, 44, 24, 7, 9, 89, 112);
    }

    @Test
    void testSeqFiler() {
        var res = this.integerSeq.filter(element -> element < 13).toList();
        assertThat(res).containsExactly(12, 7, 9);
    }

    @Test
    void testSeqMap() {
        var res = this.integerSeq.map(element -> element > 12).toList();
        assertThat(res).containsExactly(false, true, true, false, false, true, true);
    }

    @Test
    void testSeqFlatMap() {
        var a = Arrays.asList(1, 2, 3);
        var b = Arrays.asList(4, 5);
        var c = Arrays.asList(6, 7, 8, 9);

        var res = Seq.of(a, b, c).flatMap(Seq::of).toList();
        assertThat(res).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    void testSeqGenerate() {
        var random = new Random();
        var res = Seq.generate(random::nextInt).limit(10).toList();
        assertThat(res.size()).isEqualTo(10);
    }

    @Test
    void testSeqLimit() {
        var res = this.integerSeq.limit(4).toList();
        assertThat(res).containsExactly(12, 42, 24, 7);
    }

    @Test
    void testSeqLimitException() {
        assertThatThrownBy(() -> this.integerSeq.limit(-1))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSeqLimitWithMaxSizeGreaterThenCount() {
        var res = this.integerSeq.limit(12).toList();
        assertThat(res).containsExactlyElementsOf(this.integers);
    }

    @Test
    void testSeqSkip() {
        var res = this.integerSeq.skip(4).toList();
        assertThat(res).containsExactly(9, 89, 112);
    }

    @Test
    void testSeqSkipException() {
        assertThatThrownBy(() -> this.integerSeq.skip(-1))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSeqMin() {
        var res = this.integerSeq.min(Integer::compareTo);
        assertThat(res).isPresent();
        assertThat(res).get().isEqualTo(7);
    }

    @Test
    void testSeqMinWithEmptySeq() {
        var res = this.emptySeq.min(Integer::compareTo);
        assertThat(res).isEmpty();
    }

    @Test
    void testSeqMax() {
        var res = this.integerSeq.max(Integer::compareTo);
        assertThat(res).isPresent();
        assertThat(res).get().isEqualTo(7);
    }

    @Test
    void testSeqMaxWithEmptySeq() {
        var res = this.emptySeq.max(Integer::compareTo);
        assertThat(res).isEmpty();
    }

    @Test
    void testSeqIntRangeAscending() {
        var res = Seq.range(5, 10).toList();
        assertThat(res).containsExactly(5, 6, 7, 8, 9, 10);
    }

    @Test
    void testSeqIntRangeDescending() {
        var res = Seq.range(10, 5, 2).toList();
        assertThat(res).containsExactly(10, 8, 6, 5);
    }

    @Test
    void testSeqDoubleRangeAscending() {
        var res = Seq.range(5.0, 10.0).toList();
        assertThat(res).containsExactly(5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
    }

    @Test
    void testSeqDoubleRangeDescending() {
        var res = Seq.range(10.0, 5.0, 0.5).toList();
        assertThat(res).containsExactly(10.0, 9.5, 9.0, 8.5, 8.0, 7.5, 7.0, 6.5, 6.0, 5.5, 5.0);
    }

    @Test
    void testSeqCharRangeAscending() {
        var res = Seq.range('a', 'z').toList();
        assertThat(res)
            .containsExactly('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o',
                'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
    }

    @Test
    void testSeqCharRangeDescending() {
        var res = Seq.range('z', 'a', 2).toList();
        assertThat(res)
            .containsExactly('z', 'x', 'v', 't', 'r', 'p', 'n', 'l', 'j', 'h', 'f', 'd', 'b', 'a');
    }

    @Test
    void testSeqPeek() {
        var res = this.integerSeq.peek(System.out::println).toList();
        assertThat(res).containsExactlyElementsOf(this.integers);
    }

    @Test
    void testSeqAnyMatch() {
        assertThat(this.integerSeq.anyMatch(element -> element == 7)).isTrue();
        assertThat(this.integerSeq.anyMatch(element -> element == 15)).isFalse();
    }

    @Test
    void testSeqAllMatch() {
        assertThat(this.integerSeq.allMatch(element -> element > 0)).isTrue();
    }

    @Test
    void testSeqNoneMatch() {
        assertThat(this.integerSeq.noneMatch(element -> element > 200)).isTrue();
    }

    @Test
    void testSeqJoining() {
        assertThat(this.integerSeq.joining()).isEqualTo("1242247989112");
    }

    @Test
    void testSeqJoiningWithDelimiter() {
        assertThat(this.integerSeq.joining(", ")).isEqualTo("12, 42, 24, 7, 9, 89, 112");
    }

    @Test
    void testSeqReduceBinaryOperator() {
        var res = this.integerSeq.reduce(Integer::sum);
        assertThat(res).isPresent();
        assertThat(res).get().isEqualTo(295);
    }

    @Test
    void testSeqReduceBinaryOperatorWithEmptySeq() {
        var res = this.emptySeq.reduce(Integer::sum);
        assertThat(res).isEmpty();
    }

    @Test
    void testSeqReduceBinaryOperatorWithIdentity() {
        var res = this.integerSeq.reduce(5, Integer::sum);
        assertThat(res).isEqualTo(300);
    }

    @Test
    void testSeqSorted() {
        var res = this.integerSeq.sorted().toList();
        assertThat(res).containsExactly(7, 9, 12, 24, 42, 89, 112);
    }

    @Test
    void testSeqSortedWithoutComparable() {
        var list = List.of(Optional.of(12), Optional.of(42));
        var res = Seq.of(list).sorted().toList();
        assertThat(res).containsExactlyElementsOf(list);
    }

    @Test
    void testSeqReversed() {
        var res = this.integerSeq.reversed().toList();
        assertThat(res).containsExactly(112, 89, 9, 7, 24, 42, 12);
    }

    @Test
    void testSeqDistinct() {
        var res = Seq.of(12, 24, 42, 12, 12, 24, 16).distinct().toList();
        assertThat(res).containsExactly(12, 24, 42, 16);
    }

}
