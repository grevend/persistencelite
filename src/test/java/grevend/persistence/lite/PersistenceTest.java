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
