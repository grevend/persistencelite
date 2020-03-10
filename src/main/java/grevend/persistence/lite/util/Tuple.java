package grevend.persistence.lite.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Tuple {

    private final List<Object> elements;

    private Tuple() {
        this.elements = new ArrayList<>();
    }

    private Tuple(@NotNull Collection<Object> elements) {
        this.elements = new ArrayList<>();
        this.elements.addAll(elements);
    }

    @Contract("_ -> new")
    public static @NotNull Tuple of(@NotNull Collection<Object> elements) {
        return new Tuple(elements);
    }

    @Contract("_ -> new")
    public static @NotNull Tuple of(Object... elements) {
        return of(List.of(elements));
    }

    @SuppressWarnings("unchecked")
    public <A> A get(int index, @NotNull Class<A> clazz) {
        return (A) elements.get(index);
    }

    public int count() {
        return this.elements.size();
    }

    public @NotNull List<Object> getElements() {
        return elements;
    }

}
