package grevend.persistence.lite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grevend.persistence.lite.database.inmemory.InMemoryDatabase;
import org.junit.jupiter.api.Test;

class PersistenceTest {

  @Test
  void testDatabaseBuilder() {
    try(var db1 = Persistence.databaseBuilder(InMemoryDatabase.class, "db1", 0)
        .setCredentials("user", "password").build()) {
      assertThat(db1).isExactlyInstanceOf(InMemoryDatabase.class);
      assertThat(db1.getName()).isEqualTo("db1");
      assertThat(db1.getVersion()).isEqualTo(0);
      assertThat(db1.getUser()).isEqualTo("user");
      assertThat(db1.getPassword()).isEqualTo("password");
      assertThat(db1.getURI().toString()).endsWith("db1.ser");
    } catch (Exception e) {
      throw e;
    }
  }

  @Test
  void testDatabaseBuilderException() {
    assertThatThrownBy(() -> Persistence.databaseBuilder(InMemoryDatabase.class, "db1", 0).build());
  }

}
