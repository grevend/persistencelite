package grevend.persistence.lite.extensions.sql;

import grevend.persistence.lite.Persistence;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.extension.Extension;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;

public class Sql extends Extension<SqlDatabase> {

    public Sql(@NotNull Persistence persistence) {
        super(persistence);
    }

    @Override
    public @NotNull URI getURI() throws URISyntaxException {
        return new URI("jdbc:postgresql://localhost/");
    }

    @Override
    public @NotNull DaoFactory getDaoFactory() {
        return new SqlDaoFactory(this.getDatabase());
    }

    @Override
    protected @NotNull SqlDatabase createDatabase() throws IllegalStateException {
        Persistence persistence = this.getPersistence();
        return new SqlDatabase(this, persistence.getName(), persistence.getVersion(), persistence.getUser(),
                persistence.getPassword());
    }

}
