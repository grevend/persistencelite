package grevend.persistence.lite.database.sql;

import static grevend.persistence.lite.util.Utils.Crud.CREATE;
import static grevend.persistence.lite.util.Utils.Crud.RETRIEVE_ALL;
import static grevend.persistence.lite.util.Utils.Crud.RETRIEVE_BY_KEY;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.entity.EntityConstructionException;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import grevend.persistence.lite.util.Utils.Crud;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class SqlDatabase extends Database {

  private final String user, password;
  private Map<EntityClass<?>, Map<Crud, PreparedStatement>> preparedStatements;

  public SqlDatabase(@NotNull String name, int version, @NotNull String user,
      @NotNull String password) {
    super(name, version);
    this.preparedStatements = new HashMap<>();
    this.user = user;
    this.password = password;
  }

  public @NotNull String getUser() {
    return this.user;
  }

  public @NotNull String getPassword() {
    return this.password;
  }

  @Override
  public @NotNull URI getURI() throws URISyntaxException {
    return new URI("jdbc:postgresql://localhost/");
  }

  private @NotNull <E> PreparedStatement prepareCreateStatement(@NotNull EntityClass<E> entityClass)
      throws SQLException, URISyntaxException {
    return this.createConnection().prepareStatement(
        "insert into " + entityClass.getEntityName() + " (" + String
            .join(", ", entityClass.getAttributeNames()) + ") values (" + String
            .join(", ", Collections.nCopies(entityClass.getAttributeNames().size(), "?")) + ")");
  }

  private @NotNull <E> PreparedStatement prepareRetrieveWithAttributesStatement(
      @NotNull EntityClass<E> entityClass, @NotNull Collection<String> attributes)
      throws SQLException, URISyntaxException {
    return this.createConnection().prepareStatement(
        "select * from " + entityClass.getEntityName() + " where " + attributes.stream()
            .map(attribute -> attribute + "=?").collect(Collectors.joining(", ")));
  }

  @Override
  public @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entityClass,
      @NotNull List<Triplet<Class<?>, String, String>> keys) {
    if (!this.preparedStatements.containsKey(entityClass)) {
      this.preparedStatements.put(entityClass, Map.of());
    }
    return new Dao<>() {

      @Override
      public boolean create(@NotNull A entity) {
        try {
          var connection = SqlDatabase.this.createConnection();
          connection.setAutoCommit(false);
          if (!SqlDatabase.this.preparedStatements.get(entityClass).containsKey(CREATE)) {
            SqlDatabase.this.preparedStatements.put(entityClass,
                Map.of(CREATE, SqlDatabase.this.prepareCreateStatement(entityClass)));
          }
          var statement = SqlDatabase.this.preparedStatements.get(entityClass).get(CREATE);
          var values = entityClass.getAttributeValues(entity);
          for (var i = 0; i < values.size(); i++) {
            try {
              if (values.get(i) == null || values.get(i).equals("null")) {
                statement.setNull(i + 1, Types.NULL);
              } else {
                statement.setObject(i + 1, values.get(i));
              }
            } catch (SQLException ignored) {
              return false;
            }
          }
          statement.executeUpdate();
          connection.commit();
          return true;
        } catch (SQLException | URISyntaxException | EntityConstructionException ignored) {
          return false;
        }
      }

      @Override
      public Optional<A> retrieveByKey(@NotNull Tuple key) {
        try {
          if (!SqlDatabase.this.preparedStatements.get(entityClass).containsKey(RETRIEVE_BY_KEY)) {
            SqlDatabase.this.preparedStatements.put(entityClass, Map.of(RETRIEVE_BY_KEY,
                SqlDatabase.this.prepareRetrieveWithAttributesStatement(entityClass,
                    keys.stream().map(Triplet::getC).collect(Collectors.toList()))));
          }
          var statement = SqlDatabase.this.preparedStatements.get(entityClass).get(RETRIEVE_BY_KEY);
          for (var i = 0; i < keys.size(); i++) {
            try {
              if (key.get(i, keys.get(i).getA()) == null || key.get(i, keys.get(i).getA())
                  .equals("null")) {
                statement.setNull(i + 1, Types.NULL);
              } else {
                statement.setObject(i + 1, key.get(i, keys.get(i).getA()));
              }
            } catch (SQLException ignored) {
              return Optional.empty();
            }
          }
          var res = statement.executeQuery();
          if (res.next()) {
            return Optional.of(entityClass.construct(res));
          } else {
            return Optional.empty();
          }
        } catch (SQLException | URISyntaxException ignored) {
          return Optional.empty();
        }
      }

      @Override
      public Collection<A> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
        Collection<A> entities = new ArrayList<>();
        try {
          var statement = SqlDatabase.this
              .prepareRetrieveWithAttributesStatement(entityClass, attributes.keySet());
          var i = 0;
          for (Entry<String, ?> attribute : attributes.entrySet()) {
            if (attribute.getValue() == null || attribute.getValue().equals("null")) {
              statement.setNull(i + 1, Types.NULL);
            } else {
              statement.setObject(i + 1, attribute.getValue());
            }
            i++;
          }
          var res = statement.executeQuery();
          while (res.next()) {
            entities.add(entityClass.construct(res));
          }
          return entities;
        } catch (SQLException | URISyntaxException ignored) {
          return entities;
        }
      }

      @Override
      public @NotNull Collection<A> retrieveAll() {
        Collection<A> entities = new ArrayList<>();
        try {
          var connection = SqlDatabase.this.createConnection();
          if (!SqlDatabase.this.preparedStatements.get(entityClass).containsKey(RETRIEVE_ALL)) {
            SqlDatabase.this.preparedStatements.put(entityClass, Map.of(RETRIEVE_ALL,
                connection.prepareStatement("select * from " + entityClass.getEntityName())));
          }
          var res = SqlDatabase.this.preparedStatements.get(entityClass).get(RETRIEVE_ALL)
              .executeQuery();
          while (res.next()) {
            entities.add(entityClass.construct(res));
          }
          return entities;
        } catch (SQLException | URISyntaxException ignored) {
          return entities;
        }
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
    } catch (SQLException e) {
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
