package grevend.persistence.lite.util;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

import java.lang.annotation.ElementType;
import org.junit.jupiter.api.Test;

class UtilAnnotationTest {

  @Test
  void testIgnore() {
    assertAnnotationRetentionAndTarget(Ignore.class, ElementType.FIELD);
  }

}
