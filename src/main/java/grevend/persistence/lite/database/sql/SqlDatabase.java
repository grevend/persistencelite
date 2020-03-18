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

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import grevend.persistence.lite.util.function.ThrowingBiConsumer;
import grevend.persistence.lite.util.function.ThrowingBiFunction;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class SqlDatabase extends Database {

  private final String user, password;

  public SqlDatabase(@NotNull String name, int version, @NotNull String user,
      @NotNull String password) {
    super(name, version);
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

  private @NotNull <E> PreparedStatement prepareCreateStatement(@NotNull Connection connection,
      @NotNull EntityClass<E> entityClass) throws SQLException, URISyntaxException {
    return connection.prepareStatement(
        "insert into " + entityClass.getEntityName() + " (" + String
            .join(", ", entityClass.getAttributeNames()) + ") values (" + String
            .join(", ", Collections.nCopies(entityClass.getAttributeNames().size(), "?")) + ")");
  }

  private @NotNull <E> PreparedStatement prepareRetrieveWithAttributesStatement(
      @NotNull Connection connection, @NotNull EntityClass<E> entityClass,
      @NotNull Collection<String> attributes) throws SQLException, URISyntaxException {
    return connection.prepareStatement(
        "select * from " + entityClass.getEntityName() + " where " + attributes.stream()
            .map(attribute -> attribute + "=?").collect(Collectors.joining(", ")));
  }

  private @NotNull <E> PreparedStatement prepareDeleteWithAttributesStatement(
      @NotNull Connection connection, @NotNull EntityClass<E> entityClass,
      @NotNull Collection<String> attributes) throws SQLException, URISyntaxException {
    return connection.prepareStatement(
        "delete from " + entityClass.getEntityName() + " where " + attributes.stream()
            .map(attribute -> attribute + "=?").collect(Collectors.joining(", ")));
  }

  @Override
  public @NotNull <E> Dao<E> createDao(@NotNull EntityClass<E> entityClass,
      @NotNull List<Triplet<Class<?>, String, String>> keys) {

    return new Dao<>() {

      private boolean operation(
          @NotNull ThrowingBiFunction<Connection, EntityClass<E>, PreparedStatement> statementFunction,
          @NotNull ThrowingBiConsumer<Connection, PreparedStatement> operationFunction) {
        Connection connection = null;
        try {
          connection = SqlDatabase.this.createConnection();
          var statement = statementFunction.apply(connection, entityClass);
          operationFunction.accept(connection, statement);
          return true;
        } catch (Exception ignored) {
        } finally {
          if (connection != null) {
            try {
              connection.close();
            } catch (SQLException e) {
              e.printStackTrace();
            }
          }
        }
        return false;
      }

      private boolean operationWithRollback(
          @NotNull ThrowingBiFunction<Connection, EntityClass<E>, PreparedStatement> statementFunction,
          @NotNull ThrowingBiConsumer<Connection, PreparedStatement> operationFunction) {
        Connection connection = null;
        try {
          connection = SqlDatabase.this.createConnection();
          connection.setAutoCommit(false);
          var statement = statementFunction.apply(connection, entityClass);
          operationFunction.accept(connection, statement);
          return true;
        } catch (Exception e) {
          if (connection != null) {
            try {
              connection.rollback();
            } catch (SQLException sqlException) {
              sqlException.printStackTrace();
            }
          }
        } finally {
          if (connection != null) {
            try {
              connection.close();
              connection.setAutoCommit(true);
            } catch (SQLException e) {
              e.printStackTrace();
            }
          }
        }
        return false;
      }

      private void create(@NotNull E entity, @NotNull Connection connection,
          @NotNull PreparedStatement statement) throws SQLException {
        var attributes = entityClass.getAttributeValues(entity, false);
        var i = 0;
        for (String attribute : entityClass.getAttributeNames()) {
          if (attributes.get(attribute) == null || attributes.get(attribute).equals("null")) {
            statement.setNull(i + 1, Types.NULL);
          } else {
            statement.setObject(i + 1, attributes.get(attribute));
          }
          i++;
        }
        statement.executeUpdate();
      }

      @Override
      public boolean create(@NotNull E entity) {
        return this.operation(SqlDatabase.this::prepareCreateStatement, (connection, statement) -> {
          this.create(entity, connection, statement);
        });
      }

      @Override
      public boolean createAll(@NotNull Collection<E> entities) {
        return this.operationWithRollback(SqlDatabase.this::prepareCreateStatement,
            (connection, statement) -> {
              for (E entity : entities) {
                this.create(entity, connection, statement);
                connection.commit();
              }
            });
      }

      @Override
      public Optional<E> retrieveByKey(@NotNull Tuple key) {
        try (var connection = SqlDatabase.this.createConnection(); var statement = SqlDatabase.this
            .prepareRetrieveWithAttributesStatement(connection, entityClass,
                keys.stream().map(Triplet::getC).collect(Collectors.toList()))) {
          for (var i = 0; i < keys.size(); i++) {
            if (key.get(i, keys.get(i).getA()) == null || key.get(i, keys.get(i).getA())
                .equals("null")) {
              statement.setNull(i + 1, Types.NULL);
            } else {
              statement.setObject(i + 1, key.get(i, keys.get(i).getA()));
            }
          }
          var res = statement.executeQuery();
          return res.next() ? Optional.of(entityClass.construct(res)) : Optional.empty();
        } catch (SQLException | URISyntaxException ignored) {
          return Optional.empty();
        }
      }

      @Override
      public Collection<E> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
        Collection<E> entities = new ArrayList<>();
        try (var connection = SqlDatabase.this.createConnection(); var statement = SqlDatabase.this
            .prepareRetrieveWithAttributesStatement(connection, entityClass, attributes.keySet())) {
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
      public @NotNull Collection<E> retrieveAll() {
        Collection<E> entities = new ArrayList<>();
        try (var connection = SqlDatabase.this.createConnection(); var statement = connection
            .prepareStatement("select * from " + entityClass.getEntityName())) {
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
      public boolean delete(@NotNull E entity) {
        try (var connection = SqlDatabase.this.createConnection(); var statement = SqlDatabase.this
            .prepareDeleteWithAttributesStatement(connection, entityClass,
                keys.stream().map(Triplet::getC).collect(Collectors.toList()))) {
          connection.setAutoCommit(false);
          var attributes = entityClass.getAttributeValues(entity, true);
          var i = 0;
          for (String attribute : keys.stream().map(Triplet::getC).collect(Collectors.toList())) {
            if (attributes.get(attribute) == null || attributes.get(attribute).equals("null")) {
              statement.setNull(i + 1, Types.NULL);
            } else {
              statement.setObject(i + 1, attributes.get(attribute));
            }
            i++;
          }
          statement.executeUpdate();
          connection.commit();
          return true;
        } catch (SQLException | URISyntaxException ignored) {
          return false;
        }
      }

      @Override
      public boolean deleteByKey(@NotNull Tuple key) {
        try (var connection = SqlDatabase.this.createConnection(); var statement = SqlDatabase.this
            .prepareDeleteWithAttributesStatement(connection, entityClass,
                keys.stream().map(Triplet::getC).collect(Collectors.toList()))) {
          connection.setAutoCommit(false);
          for (var i = 0; i < keys.size(); i++) {
            if (key.get(i, keys.get(i).getA()) == null || key.get(i, keys.get(i).getA())
                .equals("null")) {
              statement.setNull(i + 1, Types.NULL);
            } else {
              statement.setObject(i + 1, key.get(i, keys.get(i).getA()));
            }
          }
          statement.executeUpdate();
          connection.commit();
          return true;
        } catch (SQLException | URISyntaxException ignored) {
          return false;
        }
      }

      @Override
      public boolean deleteByAttributes(@NotNull Map<String, ?> attributes) {
        try (var connection = SqlDatabase.this.createConnection(); var statement = SqlDatabase.this
            .prepareDeleteWithAttributesStatement(connection, entityClass, attributes.keySet())) {
          connection.setAutoCommit(false);
          var i = 0;
          for (Entry<String, ?> attribute : attributes.entrySet()) {
            if (attribute.getValue() == null || attribute.getValue().equals("null")) {
              statement.setNull(i + 1, Types.NULL);
            } else {
              statement.setObject(i + 1, attribute.getValue());
            }
            i++;
          }
          statement.executeUpdate();
          connection.commit();
          return true;
        } catch (SQLException | URISyntaxException ignored) {
          return false;
        }
      }

      @Override
      public @NotNull Database getDatabase() {
        return SqlDatabase.this;
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
