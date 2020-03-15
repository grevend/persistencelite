/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistence.lite.util;

import grevend.jacoco.Generated;
import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Triplet<A extends Serializable, B extends Serializable, C extends Serializable> implements
    Serializable {

  private static final long serialVersionUID = 2550349264294704474L;

  private final A a;
  private final B b;
  private final C c;

  private Triplet(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  @Contract(value = "_, _, _ -> new", pure = true)
  public static @NotNull <A extends Serializable, B extends Serializable, C extends Serializable> Triplet<A, B, C> of(
      A a, B b, C c) {
    return new Triplet<>(a, b, c);
  }

  public A getA() {
    return this.a;
  }

  public B getB() {
    return this.b;
  }

  public C getC() {
    return this.c;
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
    Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
    return Objects.equals(this.getA(), triplet.getA()) &&
        Objects.equals(this.getB(), triplet.getB()) &&
        Objects.equals(this.getC(), triplet.getC());
  }

  @Override
  @Generated
  public int hashCode() {
    return Objects.hash(this.getA(), this.getB(), this.getC());
  }

  @Override
  public String toString() {
    return "Triplet{"
        + "a=" + Utils.stringify(this.a)
        + ", b=" + Utils.stringify(this.b)
        + ", c=" + Utils.stringify(this.c)
        + '}';
  }

}
