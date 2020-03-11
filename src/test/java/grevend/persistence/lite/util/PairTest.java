package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void testPairWithArraysToString() {
        var pair = Pair.of(new Option<?>[]{Option.of(12)}, new Option<?>[]{Option.of(21)});
        assertThat(pair.toString()).isEqualTo("Pair{a=[Option[12]], b=[Option[21]]}");
    }

    @Test
    void testPairWithPrimitiveArraysToString() {
        var pair = Pair.of(new int[]{12, 42}, new long[]{21, 24});
        assertThat(pair.toString()).contains("Pair{a=[I");
        assertThat(pair.toString()).contains(", b=[J");
    }

}
