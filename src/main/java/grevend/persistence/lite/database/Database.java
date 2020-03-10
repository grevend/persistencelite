package grevend.persistence.lite.database;

import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.entity.EntityManager;
import grevend.persistence.lite.extension.Extension;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public class Database implements AutoCloseable {

    private final Extension<? extends Database> extension;

    private final String name, user, password;
    private final int version;

    private DaoFactory daoFactory;
    private EntityManager entityManager;

    public Database(@NotNull Extension<? extends Database> extension, String name, int version, String user,
                    String password) {
        this.extension = extension;
        this.name = name;
        this.version = version;
        this.user = user;
        this.password = password;
        this.start();
    }

    public @NotNull Extension<? extends Database> getExtension() {
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

    public void start() {
        this.extension.onStart();
    }

    public void stop() {
        this.extension.onStop();
    }

    @Override
    public void close() throws Exception {
        this.stop();
    }

}
