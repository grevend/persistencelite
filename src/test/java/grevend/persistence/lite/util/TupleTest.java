package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TupleTest {

    Tuple tuple = Tuple.of(12, "test", true, 42f, 128d);

    @Test
    void testTupleSize() {
        assertThat(tuple.count()).isEqualTo(5);
        assertThat(tuple.getElements().size()).isEqualTo(5);
    }

    @Test
    void testTupleGet() {
        int i = tuple.get(0, Integer.TYPE);
        String text = tuple.get(1, String.class);
        boolean b = tuple.get(2, Boolean.TYPE);
    }

    @Test
    void testTupleToString() {
        assertThat(tuple.toString()).isEqualTo("Tuple{elements=[12, test, true, 42.0, 128.0]}");
    }

}
