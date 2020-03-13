package grevend.persistence.lite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import grevend.persistence.lite.database.inmemory.InMemoryDatabase;
import grevend.persistence.lite.database.inmemory.InMemoryDatabaseBuilder;
import grevend.persistence.lite.database.sql.SqlDatabase;
import grevend.persistence.lite.database.sql.SqlDatabaseBuilder;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class PersistenceTest {

  @Test
  void testInMemoryDatabaseBuilder() {
    try (var db1 = Persistence.databaseBuilder(InMemoryDatabaseBuilder.class, "db1", 0).build()) {
      assertThat(db1).isExactlyInstanceOf(InMemoryDatabase.class);
      assertThat(db1.getName()).isEqualTo("db1");
      assertThat(db1.getVersion()).isEqualTo(0);
      assertThat(db1.getURI().toString()).endsWith("db1.ser");
    }
  }

  @Test
  void testSqlDatabaseBuilder() throws URISyntaxException {
    try (var db1 = Persistence.databaseBuilder(SqlDatabaseBuilder.class, "db1", 0)
        .setCredentials("user", "password").build()) {
      assertThat(db1).isExactlyInstanceOf(SqlDatabase.class);
      assertThat(db1.getName()).isEqualTo("db1");
      assertThat(db1.getVersion()).isEqualTo(0);
      assertThat(db1.getUser()).isEqualTo("user");
      assertThat(db1.getPassword()).isEqualTo("password");
      assertThat(db1.getURI().toString()).isEqualTo("jdbc:postgresql://localhost/");
    }
  }

  @Test
  void testDatabaseBuilderException() {
    assertThatThrownBy(
        () -> Persistence.databaseBuilder(SqlDatabaseBuilder.class, "db1", 0).build());
  }

}
