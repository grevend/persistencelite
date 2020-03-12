package grevend.persistence.lite.entity;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

import java.lang.annotation.ElementType;
import org.junit.jupiter.api.Test;

class EntityAnnotationTest {

  @Test
  void testEntity() {
    assertAnnotationRetentionAndTarget(Entity.class, ElementType.TYPE);
  }

  @Test
  void testAttribute() {
    assertAnnotationRetentionAndTarget(Attribute.class, ElementType.FIELD);
  }

}
