package grevend.persistence.lite.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.lang.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TestUtil {

    @TestOnly
    public static void assertAnnotationRetentionAndTarget(@NotNull Class<? extends Annotation> clazz,
                                                          ElementType elementType) {
        if (clazz.isAnnotationPresent(Retention.class)) {
            assertThat(clazz.getAnnotation(Retention.class).value())
                    .as(clazz.getCanonicalName() + " annotation retention policy must be RUNTIME.")
                    .isEqualTo(RetentionPolicy.RUNTIME);
        } else {
            fail("No annotation retention policy found for " + clazz.getCanonicalName() + ".");
        }
        if (clazz.isAnnotationPresent(Target.class)) {
            assertThat(clazz.getAnnotation(Target.class).value())
                    .as(clazz.getCanonicalName() + " annotation target must contain " + elementType.name() + ".")
                    .contains(elementType);
        } else {
            fail("No annotation target found for " + clazz.getCanonicalName() + ".");
        }
    }

    @TestOnly
    public static void assertAnnotationRetentionAndTarget(@NotNull Class<? extends Annotation> clazz) {
        assertAnnotationRetentionAndTarget(clazz, ElementType.METHOD);
    }

    @SafeVarargs
    @TestOnly
    public static <T> void verifyEqualsAndHashCode(@NotNull T ref, @NotNull T equal, @NotNull T... unEqual) {
        Object object = "Hello";
        T tnull = null;
        String cname = ref.getClass().getCanonicalName();
        assertThat(ref.equals(ref)).isTrue();
        assertThat(ref.equals(tnull)).isFalse();
        assertThat(ref.equals(object)).isFalse();
        assertThat(ref.equals(equal)).isTrue();
        for (int i = 0; i < unEqual.length; i++) {
            T ueq = unEqual[i];
            assertThat(ref).isNotEqualTo(ueq);
        }
        assertThat(ref.hashCode()).isEqualTo(equal.hashCode());
    }

    @TestOnly
    public static String safeToString(Object x) {
        if (x == null) {
            return "null";
        }
        try {
            return x.toString();
        } catch (Throwable e) {
            return "invoking toString on instance "
                    + x.getClass().getCanonicalName() + "@"
                    + Integer.toHexString(System.identityHashCode(x))
                    + " causes an exception " + e.toString();

        }
    }

}
