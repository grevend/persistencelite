package grevend.persistence.lite.database;

import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.entity.EntityManager;
import grevend.persistence.lite.extension.Extension;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public class Database {

    private final Extension extension;

    private final String name, user, password;
    private final int version;

    private DaoFactory daoFactory;
    private EntityManager entityManager;

    public Database(@NotNull Extension extension, String name, int version, String user, String password) {
        this.extension = extension;
        this.name = name;
        this.version = version;
        this.user = user;
        this.password = password;
    }

    public @NotNull Extension getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public URI getURI() throws URISyntaxException {
        return this.extension.getURI();
    }

    public @NotNull DaoFactory getDaoFactory() {
        if (this.daoFactory == null) {
            this.daoFactory = this.extension.getDaoFactory();
        }
        return this.daoFactory;
    }

    public @NotNull EntityManager getEntityManager() {
        if (this.entityManager == null) {
            this.entityManager = new EntityManager(this);
        }
        return this.entityManager;
    }

    /*public static final String SQL = "sql";
    public static final String MEMORY = "memory";

    private final String type, url, name, user, password;
    private final int version;

    private DaoFactory daoFactory;
    private EntityManager entityManager;

    public Database(@NotNull String type, @NotNull String url,
                    @NotNull String name, @NotNull String user, @NotNull String password, int version) {
        this.type = type;
        this.url = url;
        this.name = name;
        this.user = user;
        this.password = password;
        this.version = version;
    }

    public @NotNull String getType() {
        return type;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getUser() {
        return user;
    }

    public int getVersion() {
        return version;
    }



    public @NotNull Optional<DatabaseMetaData> getMetaData(@NotNull Connection connection) {
        if (this.type.equals(SQL)) {
            try {
                return Optional.ofNullable(connection.getMetaData());
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public Connection createConnection() throws SQLException, IllegalAccessException {
        if (this.type.equals(SQL)) {
            Properties props = new Properties();
            props.setProperty("user", this.user);
            props.setProperty("password", this.password);
            return DriverManager.getConnection(this.url + this.name, props);
        } else {
            throw new IllegalAccessException("Database type must be SQL to create an sql connection.");
        }
    }
    */

}
