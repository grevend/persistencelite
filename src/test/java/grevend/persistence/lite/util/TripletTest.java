package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TripletTest {

    @Test
    void testTripletOf() {
        var triplet = new Triplet<>("test", 45L, true);
        assertThat(triplet.getA()).isEqualTo("test");
        assertThat(triplet.getB()).isEqualTo(45L);
        assertThat(triplet.getC()).isEqualTo(true);
    }

    @Test
    void testTripletToString() {
        var triplet = new Triplet<>("test", 45L, true);
        assertThat(triplet.toString()).isEqualTo("Triplet{a=test, b=45, c=true}");
    }

    @Test
    void testTripletWithNullToString() {
        var triplet = new Triplet<>(null, null, null);
        assertThat(triplet.toString()).isEqualTo("Triplet{a=null, b=null, c=null}");
    }

    @Test
    void testTripletWithArraysToString() {
        var triplet = new Triplet<>(new Option<?>[]{Option.of(12)}, new Option<?>[]{Option.of(21)},
                new Option<?>[]{Option.of(21)});
        assertThat(triplet.toString()).isEqualTo("Triplet{a=[Option[12]], b=[Option[21]], c=[Option[21]]}");
    }

    @Test
    void testTripletWithPrimitiveArraysToString() {
        var triplet = new Triplet<>(new int[]{12, 42}, new long[]{21, 24}, new byte[]{12, 24});
        assertThat(triplet.toString()).contains("Triplet{a=[I");
        assertThat(triplet.toString()).contains(", b=[J");
        assertThat(triplet.toString()).contains(", c=[B");
    }

}
