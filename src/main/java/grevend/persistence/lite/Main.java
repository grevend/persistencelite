package grevend.persistence.lite;

import grevend.persistence.lite.database.sql.SqlDatabase;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.Entity;
import grevend.persistence.lite.util.Option;
import grevend.persistence.lite.util.PrimaryKey;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        /*try (var db = Persistence.databaseBuilder(InMemoryDatabase.class, "postgres", 0)
                .setCredentials("postgres", "mypassword")
                .build()) {
            Dao<Artist> artistDao = db.getDao(Artist.class);

            artistDao.create(new Artist(50, "test", "biotest", null, Option.empty()));

            System.out.println(artistDao.retrieveAll());
            System.out.println(artistDao.retrieveByAttributes(Map.of("id", 49)));
            Collection<Artist> artistCollection = artistDao.retrieveByAttributes(Map.of("id", 50));
            if (!artistCollection.isEmpty()) {
                Artist artist = artistCollection.iterator().next();
                System.out.println(artist);
                System.out.println(artistDao.retrieve(49));
                System.out.println(artistDao.retrieve(50));
                artist.name = "newartistname";
                artistDao.update(artist);
                System.out.println(artistDao.retrieveAll().size());
                System.out.println(artistDao.retrieve(artist.id));
                System.out.println(artistDao.retrieveAll().size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try (var db = Persistence.databaseBuilder(SqlDatabase.class, "postgres", 0)
                .setCredentials("postgres", "mypassword")
                .build()) {
            Connection connection = db.createConnection();
            System.out.println(db.getMetaData(connection).isPresent() ? db.getMetaData(connection).get().getJDBCMajorVersion() : "null");
            var dao = db.getDao(Artist.class);
            System.out.println(dao.retrieveAll());
            //System.out.println(dao.retrieveByAttributes(Map.of("id", 4)));
            //System.out.println(dao.create(new Artist(100, "100", "100-bio", Option.of(0), Option.empty())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Entity(name = "artist")
    private static class Artist implements Serializable {

        private static final long serialVersionUID = 6151647160255764536L;

        @PrimaryKey
        @Attribute(name = "id", autoGenerated = true)
        public int id;
        public String name, bio;
        public Option<Integer> verifier;
        public Option<Byte[]> image;

        protected Artist() {
        }

        public Artist(int id, String name, String bio, Option<Integer> verifier, Option<Byte[]> image) {
            this.id = id;
            this.name = name;
            this.bio = bio;
            this.verifier = verifier;
            this.image = image;
        }

        @Override
        public String toString() {
            return "Artist{" +
                    "id=" + this.id +
                    ", name='" + this.name + '\'' +
                    ", bio='" + this.bio + '\'' +
                    ", image=" + this.image +
                    ", verifier=" + this.verifier +
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
