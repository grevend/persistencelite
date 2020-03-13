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
