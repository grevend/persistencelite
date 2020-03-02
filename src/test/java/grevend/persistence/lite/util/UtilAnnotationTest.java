package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;

import static grevend.persistence.lite.util.Annotations.assertAnnotationRetentionAndTarget;

public class UtilAnnotationTest {

    @Test
    public void testIgnore() {
        assertAnnotationRetentionAndTarget(Ignore.class, ElementType.FIELD);
    }

}
