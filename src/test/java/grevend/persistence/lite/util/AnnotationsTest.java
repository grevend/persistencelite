package grevend.persistence.lite.util;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static grevend.persistence.lite.util.TestUtil.assertAnnotationRetentionAndTarget;

class AnnotationsTest {

    @Test
    void testRetentionAndTargetArePresentAndCorrect() {
        try {
            assertAnnotationRetentionAndTarget(Ignore.class, ElementType.FIELD);
        } catch (AssertionError assertionError) {
            Assertions.fail(assertionError.getMessage());
        }
    }

    @Test
    void testRetentionAndTargetAreNotPresent() {
        try {
            assertAnnotationRetentionAndTarget(NoRetentionOrTargetTest.class);
            Assertions.fail("No assertion error found. Retention and Target already set.");
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage())
                    .isEqualToIgnoringCase("No annotation retention policy found for " +
                            NoRetentionOrTargetTest.class.getCanonicalName() + ".");
        }
    }

    @Test
    void testRetentionIsNotRuntime() {
        try {
            assertAnnotationRetentionAndTarget(NotRuntimeTest.class);
            Assertions.fail("No assertion error found. Retentions is already RUNTIME.");
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage()).contains("annotation retention policy must be RUNTIME.");
        }
    }

    @Test
    void testTargetIsNotPresent() {
        try {
            assertAnnotationRetentionAndTarget(NoTargetTest.class);
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage()).startsWith("No annotation target found for");
        }
    }

    @Test
    void testTargetIsNotField() {
        try {
            assertAnnotationRetentionAndTarget(MethodTargetTest.class, ElementType.FIELD);
            Assertions.fail("No assertion error found. Target is already FIELD.");
        } catch (AssertionError assertionError) {
            Assertions.assertThat(assertionError.getMessage()).contains("annotation target must contain FIELD.");
        }
    }

    @TestOnly
    @interface NoRetentionOrTargetTest {
    }

    @TestOnly
    @Retention(RetentionPolicy.SOURCE)
    @interface NotRuntimeTest {
    }

    @TestOnly
    @Retention(RetentionPolicy.RUNTIME)
    @interface NoTargetTest {
    }

    @TestOnly
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface MethodTargetTest {
    }

}
