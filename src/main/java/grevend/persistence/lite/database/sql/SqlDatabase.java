package grevend.persistence.lite.database.sql;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

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
    public @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entity,
                                         @NotNull List<Triplet<Class<?>, String, String>> keys) {
        return new Dao<>() {

            @Override
            public boolean create(@NotNull A entity) {
                return false;
            }

            @Override
            public Optional<A> retrieveByKey(@NotNull Tuple key) {
                return Optional.empty();
            }

            @Override
            public Collection<A> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
                return null;
            }

            @Override
            public @NotNull Collection<A> retrieveAll() {
                Collection<A> entities = new ArrayList<>();
                try {
                    ResultSet res = createConnection().createStatement().executeQuery("select * from " + entity.getEntityName());
                    while(res.next()) {
                        entities.add(entity.construct(res));
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
