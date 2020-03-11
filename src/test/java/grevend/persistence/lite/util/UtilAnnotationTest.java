package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

class UtilAnnotationTest {

    @Test
    void testIgnore() {
        assertAnnotationRetentionAndTarget(Ignore.class, ElementType.FIELD);
    }

}
