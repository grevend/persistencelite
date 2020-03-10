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
    public static <T> void verifyEqualsAndHashCode(@NotNull T ref, @NotNull T equal, @NotNull T... unEqual ) {
        Object object = "Hello";
        T tnull = null;
        String cname = ref.getClass().getCanonicalName();
        // I got bitten here, assertJ equalTo does not invoke equals on the
        // object when ref and 'other' are same.
        // THAT's why the first one differs from the rest.
        assertThat( ref.equals( ref ) )
                .as( cname + ".equals(this): with self should produce true" )
                .isTrue();
        assertThat( ref.equals( tnull ) )
                .as( cname + ".equals(null): ref object "
                        + safeToString( ref ) + " and null should produce false"
                )
                .isFalse();
        assertThat( ref.equals( object ) )
                .as( cname + ".equals(new Object()): ref object"
                        + " compared to other type should produce false"
                )
                .isFalse();
        assertThat( ref.equals( equal ) )
                .as( cname + " ref object [" + safeToString( ref )
                        + "] and equal object [" + safeToString( equal )
                        + "] should report equal"
                )
                .isTrue();
        for ( int i = 0; i < unEqual.length; i++ ) {
            T ueq = unEqual[ i ];
            assertThat( ref )
                    .as("testing supposed unequal objects")
                    .isNotEqualTo( ueq );
        }
        // ref and equal should have same hashCode
        assertThat( ref.hashCode() )
                .as( cname + " equal objects "
                        + ref.toString() + " and "
                        + equal.toString() + " should have same hashcode"
                )
                .isEqualTo( equal.hashCode() );
    }

    @TestOnly
    public static String safeToString( Object x ) {
        if ( x == null ) {
            return "null";
        }
        try {
            return x.toString();
        } catch ( Throwable e ) {
            return "invoking toString on instance "
                    + x.getClass().getCanonicalName() + "@"
                    + Integer.toHexString( System.identityHashCode( x ) )
                    + " causes an exception " + e.toString();

        }
    }

}
