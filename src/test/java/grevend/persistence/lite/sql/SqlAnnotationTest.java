package grevend.persistence.lite.sql;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

import grevend.persistence.lite.util.PrimaryKey;
import java.lang.annotation.ElementType;
import org.junit.jupiter.api.Test;

class SqlAnnotationTest {

  @Test
  void testPrimaryKey() {
    assertAnnotationRetentionAndTarget(PrimaryKey.class, ElementType.FIELD);
  }

}