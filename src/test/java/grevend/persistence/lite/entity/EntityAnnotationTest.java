package grevend.persistence.lite.entity;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;

import static grevend.persistence.lite.util.Annotations.assertAnnotationRetentionAndTarget;

public class EntityAnnotationTest {

    @Test
    public void testEntity() {
        assertAnnotationRetentionAndTarget(Entity.class, ElementType.TYPE);
    }

    @Test
    public void testAttribute() {
        assertAnnotationRetentionAndTarget(Attribute.class, ElementType.FIELD);
    }

}
