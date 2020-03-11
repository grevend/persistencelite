package grevend.persistence.lite.entity;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

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
