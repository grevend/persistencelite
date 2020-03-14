package grevend.persistence.lite.util;

import grevend.jacoco.Generated;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Tuple {

  private final List<Object> elements;

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
    return (A) this.elements.get(index);
  }

  public int count() {
    return this.elements.size();
  }

  public @NotNull List<Object> getElements() {
    return this.elements;
  }

  @Override
  @Generated
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    Tuple tuple = (Tuple) o;
    return Objects.equals(this.getElements(), tuple.getElements());
  }

  @Override
  @Generated
  public int hashCode() {
    return Objects.hash(this.getElements());
  }

  @Override
  public String toString() {
    return "Tuple{" +
        "elements=" + this.elements +
        '}';
  }

}
