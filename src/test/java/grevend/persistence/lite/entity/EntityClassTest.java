package grevend.persistence.lite.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grevend.persistence.lite.util.Ignore;
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

}
