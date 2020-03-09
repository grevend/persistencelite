package grevend.persistence.lite.extensions.sql;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.extension.Extension;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class SqlDatabase extends Database {

    public SqlDatabase(@NotNull Extension<? extends Database> extension,
                       String name, int version, String user, String password) {
        super(extension, name, version, user, password);
    }

    public @NotNull Optional<DatabaseMetaData> getMetaData(@NotNull Connection connection) {
        try {
            return Optional.ofNullable(connection.getMetaData());
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return Optional.empty();
        }
    }

    public Connection createConnection() throws SQLException, IllegalAccessException, URISyntaxException {
        Properties props = new Properties();
        props.setProperty("user", this.getUser());
        props.setProperty("password", this.getPassword());
        return DriverManager.getConnection(this.getURI().toString() + this.getName(), props);
    }

}
