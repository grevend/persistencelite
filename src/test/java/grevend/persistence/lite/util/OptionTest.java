package grevend.persistence.lite.util;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    @Test
    void testOptionOf() {
        Option<Integer> integerOption = Option.of(12);
        assertThat(integerOption.isPresent()).isTrue();
        assertThat(integerOption.isEmpty()).isFalse();
        assertThat(integerOption.get()).isEqualTo(12);
    }

    @Test
    void testOptionOfNull() {
        Option<Integer> integerOption = Option.of(null);
        assertThat(integerOption.isPresent()).isFalse();
        assertThat(integerOption.isEmpty()).isTrue();
        assertThatThrownBy(integerOption::get).isExactlyInstanceOf(NoSuchElementException.class);
    }

    @Test
    void testOptionFromOptional() {
        Option<Integer> integerOption = Option.from(Optional.of(12));
        assertThat(integerOption.isPresent()).isTrue();
        assertThat(integerOption.isEmpty()).isFalse();
        assertThat(integerOption.get()).isEqualTo(12);
    }

    @Test
    void testOptionFromEmptyOptional() {
        Option<Integer> integerOption = Option.from(Optional.empty());
        assertThat(integerOption.isPresent()).isFalse();
        assertThat(integerOption.isEmpty()).isTrue();
        assertThatThrownBy(integerOption::get).isExactlyInstanceOf(NoSuchElementException.class);
    }

    @Test
    void testOptionToString() {
        Option<Integer> integerOption = Option.of(42);
        assertThat(integerOption.toString()).isEqualTo("Option[42]");
    }

    @Test
    void testOptionOfNullToString() {
        assertThat(Option.of(null).toString()).isEqualTo("Option.empty");
    }

}
