package grevend.persistence.lite;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.Entity;
import grevend.persistence.lite.util.AutoGenerated;
import grevend.persistence.lite.util.PrimaryKey;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        Database db = Persistence.databaseBuilder("postgres", 0)
                .setCredentials("postgres", "mypassword")
                .build();

        try (Connection connection = db.createConnection()) {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM artist");
            while (rs.next()) {
                System.out.println(db.getEntityManager().constructEntity(Artist.class, rs));
            }
            rs.close();
            st.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        var artistDao = db.getDaoFactory().ofEntity(Artist.class);
        System.out.println(artistDao.retrieve(0));
        List<Artist> artists = artistDao.retrieveAll();
        System.out.println(artists);

        Dao<Artist, Integer> d1 = db.getDaoFactory().ofEntity(Artist.class);
        var d2 = db.getDaoFactory().ofEntity(Artist.class);

        Dao<Artist2, List<Class<?>>> d3 = db.getDaoFactory().ofEntity(Artist2.class);
        var d4 = db.getDaoFactory().ofEntity(Artist2.class);
    }

    @Entity(name = "artist")
    private static class Artist {

        @PrimaryKey
        @AutoGenerated
        @Attribute(name = "id")
        public int id;
        public String name, bio;
        public Object verifier;
        public Optional<Object> image;

        protected Artist() {
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
