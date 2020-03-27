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

package grevend.persistencelite.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grevend.persistencelite.util.Ignore;
import java.io.Serializable;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EntityClassTest {

  @Test
  void testEntityClassOf() throws IllegalArgumentException {
    EntityClass<DummyEntity> dummyEntityEntityClass = EntityClass.of(DummyEntity.class);
    assertThat(dummyEntityEntityClass.getEntityClass()).isEqualTo(DummyEntity.class);
    assertThat(EntityClass.of(DummyEntity.class)).isNotEqualTo(DummyEntity2.class);
  }

  @Test
  void testEntityClassOfArgumentException() {
    assertThatThrownBy(() -> EntityClass.of(Integer.class))
        .isExactlyInstanceOf(EntityImplementationException.class);
  }

  @Test
  void testEntityClassGetEntityName() {
    assertThat(EntityClass.of(DummyEntity.class).getEntityName()).isEqualTo("dummy");
    assertThat(EntityClass.of(DummyEntity.class).getEntityName()).isNotEqualTo("dummy2");
  }

  @Test
  void testIfEntityIsSerializable() {
    assertThat(EntityClass.of(DummyEntity.class).isSerializable()).isFalse();
    assertThat(EntityClass.of(DummyEntity4.class).isSerializable()).isTrue();
  }

  @Test
  void testEntityHasViableConstructor() {
    assertThat(EntityClass.of(DummyEntity.class).hasViableConstructor()).isFalse();
    assertThat(EntityClass.of(DummyEntity.class).hasViableConstructor()).isFalse();
    assertThat(EntityClass.of(DummyEntity2.class).hasViableConstructor()).isTrue();
  }

  @Test
  void testEntityHasViableFields() {
    assertThat(EntityClass.of(DummyEntity.class).hasViableFields()).isFalse();
    assertThat(EntityClass.of(DummyEntity.class).hasViableFields()).isFalse();
    assertThat(EntityClass.of(DummyEntity2.class).hasViableFields()).isFalse();
    assertThat(EntityClass.of(DummyEntity3.class).hasViableFields()).isTrue();
  }

  @Test
  void testEntityConstruct() {
    var dummyEntity = EntityClass.of(DummyEntity2.class).construct(Map.of("id", 12));
    assertThat(dummyEntity).isNotNull();
  }

  @Test
  void testEntityConstructStateException() {
    assertThatThrownBy(() -> EntityClass.of(DummyEntity.class).construct(Map.of("id", 12)))
        .isExactlyInstanceOf(EntityImplementationException.class).withFailMessage(
        "Class grevend.persistence.lite.entity.EntityClassTest.DummyEntity must declare an constructor.");
  }

  @Test
  void testEntityConstructWithField() {
    var dummyEntity = EntityClass.of(DummyEntity3.class).construct(Map.of("id", 12));
    assertThat(dummyEntity).isNotNull();
  }

  @Test
  void testEntityHasNoArgsConstructor() {
    assertThat(EntityClass.of(DummyEntity.class).hasViableNoArgsConstructor()).isFalse();
    assertThat(EntityClass.of(DummyEntity2.class).hasViableNoArgsConstructor()).isTrue();
  }

  @Test
  void testEntityHasArgsConstructor() {
    EntityClass.EntityClassCache.getInstance().clearCache();
    assertThat(EntityClass.of(DummyEntity.class).hasViableArgsConstructor()).isFalse();
    assertThat(EntityClass.of(DummyEntity2.class).hasViableArgsConstructor()).isTrue();
    assertThat(EntityClass.of(DummyEntity3.class).hasViableArgsConstructor()).isFalse();
    assertThat(EntityClass.of(DummyEntity5.class).hasViableArgsConstructor()).isTrue();
  }

  @Entity(name = "dummy")
  private static class DummyEntity {

    @Ignore
    private long test;

    protected DummyEntity(long test) {
      this.test = test;
    }
  }

  @Entity(name = "dummy2")
  private static class DummyEntity2 {

  }

  //TODO add support for modifiers other than public!
  @Entity(name = "dummy3")
  public static class DummyEntity3 {

    public int id;

  }

  @Entity(name = "dummy4")
  private static class DummyEntity4 implements Serializable {

  }

  @Entity(name = "dummy5")
  private static class DummyEntity5 {

    private long test;
    private int id;

    public DummyEntity5(long test, int id) {
      this.id = id;
      this.test = test;
    }

  }

}
