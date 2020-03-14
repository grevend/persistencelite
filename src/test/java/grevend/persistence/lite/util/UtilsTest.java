package grevend.persistence.lite.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void testStringify() {
    assertThat(Utils.stringify(12)).isEqualTo("12");
  }

  @Test
  void testStringifyNull() {
    assertThat(Utils.stringify(null)).isEqualTo("null");
  }

  @Test
  void testStringifyArray() {
    assertThat(Utils.stringify(new Option<?>[]{Option.of(12)})).isEqualTo("[Option[12]]");
  }

  @Test
  void testStringifyPrimitiveArray() {
    assertThat(Utils.stringify(new int[]{12, 42})).startsWith("[I@");
  }

}
