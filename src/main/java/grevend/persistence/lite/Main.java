package grevend.persistence.lite;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.Entity;
import grevend.persistence.lite.extensions.inmemory.InMemory;
import grevend.persistence.lite.util.AutoGenerated;
import grevend.persistence.lite.util.Option;
import grevend.persistence.lite.util.PrimaryKey;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        var db = Persistence.databaseBuilder(InMemory.class, "postgres", 0)
                .setCredentials("postgres", "mypassword")
                .build();

        Dao<Artist, Integer> artistDao = db.getDaoFactory().ofEntity(Artist.class);

        artistDao.create(new Artist(50, "test", "biotest", null, Option.empty()));

        System.out.println(artistDao.retrieveAll());
        System.out.println(artistDao.retrieve(Map.of("id", 49)));
        Optional<Artist> artist = artistDao.retrieve(Map.of("id", 50));
        System.out.println(artist);
        System.out.println(artistDao.retrieve(49));
        System.out.println(artistDao.retrieve(50));
        artist.get().name = "newartistname";
        artistDao.update(artist.get());
        System.out.println(artistDao.retrieveAll().size());
        System.out.println(artistDao.retrieve(artist.get().id));
        System.out.println(artistDao.retrieveAll().size());

    }

    @Entity(name = "artist")
    private static class Artist implements Serializable {

        private static final long serialVersionUID = 6151647160255764536L;

        @PrimaryKey
        @AutoGenerated
        @Attribute(name = "id")
        public int id;
        public String name, bio;
        public Option<Boolean> verifier;
        public Option<Byte[]> image;

        protected Artist() {
        }

        public Artist(int id, String name, String bio, Option<Boolean> verifier, Option<Byte[]> image) {
            this.id = id;
            this.name = name;
            this.bio = bio;
            this.verifier = verifier;
            this.image = image;
        }

        @Override
        public String toString() {
            return "Artist{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", bio='" + bio + '\'' +
                    ", image=" + image +
                    ", verifier=" + verifier +
                    '}';
        }

    }

    @Entity(name = "artist2")
    private static class Artist2 {

        @PrimaryKey
        public int id_int;
        @PrimaryKey
        public String id_str;

        protected Artist2() {
        }

    }

}
