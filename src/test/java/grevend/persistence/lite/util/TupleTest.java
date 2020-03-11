package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TupleTest {

    private Tuple tuple = Tuple.of(12, "test", true, 42f, 128d);

    @Test
    void testTupleSize() {
        assertThat(this.tuple.count()).isEqualTo(5);
        assertThat(this.tuple.getElements().size()).isEqualTo(5);
    }

    @Test
    void testTupleGet() {
        int i = this.tuple.get(0, Integer.TYPE);
        String text = this.tuple.get(1, String.class);
        boolean b = this.tuple.get(2, Boolean.TYPE);
    }

    @Test
    void testTupleToString() {
        assertThat(this.tuple.toString()).isEqualTo("Tuple{elements=[12, test, true, 42.0, 128.0]}");
    }

}
