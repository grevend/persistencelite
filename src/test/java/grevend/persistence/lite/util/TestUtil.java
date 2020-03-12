package grevend.persistence.lite.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

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
          .as(clazz.getCanonicalName() + " annotation target must contain " + elementType.name()
              + ".")
          .contains(elementType);
    } else {
      fail("No annotation target found for " + clazz.getCanonicalName() + ".");
    }
  }

  @TestOnly
  public static void assertAnnotationRetentionAndTarget(
      @NotNull Class<? extends Annotation> clazz) {
    assertAnnotationRetentionAndTarget(clazz, ElementType.METHOD);
  }

}
