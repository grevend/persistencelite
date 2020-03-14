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
