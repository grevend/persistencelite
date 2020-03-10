package grevend.persistence.lite.entity;

import grevend.persistence.lite.util.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static grevend.persistence.lite.util.TestUtil.verifyEqualsAndHashCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityClassTest {

    @Test
    void testEntityClassOf() throws IllegalArgumentException {
        EntityClass<DummyEntity> dummyEntityEntityClass = EntityClass.of(DummyEntity.class);
        assertThat(dummyEntityEntityClass.getEntityClass()).isEqualTo(DummyEntity.class);
        assertThat(EntityClass.of(DummyEntity.class)).isNotEqualTo(DummyEntity2.class);
    }

    @Test
    void testEntityClassOfArgumentException() {
        assertThatThrownBy(() -> EntityClass.of(Integer.class)).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEntityClassGetEntityName() {
        assertThat(EntityClass.of(DummyEntity.class).getEntityName()).isEqualTo("dummy");
        assertThat(EntityClass.of(DummyEntity.class).getEntityName()).isNotEqualTo("dummy2");
    }

    @Test
    void testEntityConstruct() {
        var dummyEntity = EntityClass.of(DummyEntity2.class).construct(Map.of("id", 12));
        assertThat(dummyEntity).isNotNull();
    }

    @Test
    void testEntityConstructStateException() {
        assertThatThrownBy(() -> EntityClass.of(DummyEntity.class).construct(Map.of("id", 12)))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasCauseExactlyInstanceOf(IllegalArgumentException.class).withFailMessage(
                "Class grevend.persistence.lite.entity.EntityClassTest.DummyEntity must declare an constructor.");
    }

    @Test
    void testEntityConstructWithField() {
        var dummyEntity = EntityClass.of(DummyEntity3.class).construct(Map.of("id", 12));
        assertThat(dummyEntity).isNotNull();
    }

    @Test
    void testEqualsAndHashCode() {
        var entityClass1 = EntityClass.of(DummyEntity.class);
        var entityClass2 = EntityClass.of(DummyEntity.class);
        var entityClass3 = EntityClass.of(DummyEntity2.class);
        verifyEqualsAndHashCode(entityClass1, entityClass2, entityClass3);
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

}
