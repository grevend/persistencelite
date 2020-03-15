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

package grevend.persistence.lite.database.sql;

import grevend.jacoco.Generated;
import grevend.persistence.lite.database.DatabaseBuilder;
import grevend.persistence.lite.database.DatabaseBuilderException;
import org.jetbrains.annotations.NotNull;

public class SqlDatabaseBuilder extends DatabaseBuilder<SqlDatabase> {

  private String user, password;

  public SqlDatabaseBuilder(@NotNull String name, int version) {
    super(name, version);
  }

  public SqlDatabaseBuilder setCredentials(@NotNull String user, @NotNull String password) {
    this.user = user;
    this.password = password;
    return this;
  }

  @Generated
  public @NotNull SqlDatabase build() {
    if (this.user == null || this.password == null) {
      throw new DatabaseBuilderException("Credentials must be set before building the database.");
    }
    return new SqlDatabase(this.name, this.version, this.user, this.password);
  }

}
