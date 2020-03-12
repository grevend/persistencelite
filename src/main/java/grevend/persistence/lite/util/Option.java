package grevend.persistence.lite.util;

import grevend.persistence.lite.util.jacoco.Generated;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class Option<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 5775395636702270419L;
    private T value;

    private Option() {
        this.value = null;
    }

    private Option(T value) {
        this.value = value;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull <T extends Serializable> Option<T> empty() {
        return new Option<T>();
    }

    @Contract("!null -> new")
    public static @NotNull <T extends Serializable> Option<T> of(T value) {
        return value == null ? empty() : new Option<>(value);
    }

    public static @NotNull <T extends Serializable> Option<T> from(@NotNull Optional<T> value) {
        return value.isEmpty() ? empty() : new Option<>(value.get());
    }

    public @NotNull T get() {
        if (this.value == null) {
            throw new NoSuchElementException("No value present");
        } else {
            return this.value;
        }
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public boolean isEmpty() {
        return this.value == null;
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Option<?> option = (Option<?>) o;
        return Objects.equals(this.value, option.value);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(this.value);
    }

    public String toString() {
        return this.value != null ? String.format("Option[%s]", this.value) : "Option.empty";
    }

}
