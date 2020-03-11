package grevend.persistence.lite.sql;

import grevend.persistence.lite.util.PrimaryKey;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

class SqlAnnotationTest {

    @Test
    void testPrimaryKey() {
        assertAnnotationRetentionAndTarget(PrimaryKey.class, ElementType.FIELD);
    }

}