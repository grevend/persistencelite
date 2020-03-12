package grevend.persistence.lite.database.sql;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class SqlDatabase extends Database {

  public SqlDatabase(@NotNull String name, int version, @NotNull String user,
      @NotNull String password) {
    super(name, version, user, password);
  }

  @Override
  public @NotNull URI getURI() throws URISyntaxException {
    return new URI("jdbc:postgresql://localhost/");
  }

  @Override
  public @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entityClass,
      @NotNull List<Triplet<Class<?>, String, String>> keys) {
    return new Dao<>() {

      @Override
      public boolean create(@NotNull A entity) {
        var query = "insert into " + entityClass.getEntityName() + " (" +
            String.join(", ", entityClass.getAttributeNames()) + ") values (" +
            entityClass.getAttributeValues(entity).stream()
                .map(obj -> obj == null ? "null" : obj.toString())
                .collect(Collectors.joining(", ")) + ")";
        try {
          SqlDatabase.this.createConnection().createStatement().executeQuery(query);
          return true;
        } catch (SQLException | URISyntaxException e) {
          e.printStackTrace();
          return false;
        }
      }

      @Override
      public Optional<A> retrieveByKey(@NotNull Tuple key) {
        return Optional.empty();
      }

      @Override
      public Collection<A> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
        Collection<A> entities = new ArrayList<>();
        var query = "select * from " + entityClass.getEntityName() + " where " +
            attributes.entrySet().stream().map(entry -> entry.getKey() + "="
                + entry.getValue().toString()).collect(Collectors.joining(", "));
        try {
          ResultSet res = SqlDatabase.this.createConnection().createStatement().executeQuery(query);
          while (res.next()) {
            entities.add(entityClass.construct(res));
          }
        } catch (SQLException | URISyntaxException e) {
          e.printStackTrace();
        }
        return entities;
      }

      @Override
      public @NotNull Collection<A> retrieveAll() {
        Collection<A> entities = new ArrayList<>();
        try {
          ResultSet res = SqlDatabase.this.createConnection().createStatement()
              .executeQuery("select * from " + entityClass.getEntityName());
          while (res.next()) {
            entities.add(entityClass.construct(res));
          }
        } catch (SQLException | URISyntaxException e) {
          e.printStackTrace();
        }
        return entities;
      }

      @Override
      public boolean delete(@NotNull A entity) {
        return false;
      }

      @Override
      public boolean deleteByKey(@NotNull Tuple key) {
        return false;
      }

    };
  }

  public @NotNull Optional<DatabaseMetaData> getMetaData(@NotNull Connection connection) {
    try {
      return Optional.ofNullable(connection.getMetaData());
    } catch (SQLException sqlException) {
      sqlException.printStackTrace();
      return Optional.empty();
    }
  }

  public @NotNull Connection createConnection() throws SQLException, URISyntaxException {
    Properties props = new Properties();
    props.setProperty("user", this.getUser());
    props.setProperty("password", this.getPassword());
    return DriverManager.getConnection(this.getURI().toString() + this.getName(), props);
  }

}
