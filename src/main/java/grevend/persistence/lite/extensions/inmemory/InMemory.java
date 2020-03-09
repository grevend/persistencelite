package grevend.persistence.lite.extensions.inmemory;

import grevend.persistence.lite.Persistence;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.extension.Extension;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class InMemory extends Extension<Database> {

    public InMemory(@NotNull Persistence persistence) {
        super(persistence);
    }

    @Override
    public @NotNull URI getURI() throws URISyntaxException {
        return new File(this.getDatabase().getName() + ".ser").toURI();
    }

    @Override
    public @NotNull DaoFactory getDaoFactory() {
        return new InMemoryDaoFactory(this.getDatabase());
    }

}
