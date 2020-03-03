package grevend.persistence.lite;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.Attribute;
import grevend.persistence.lite.entity.Entity;
import grevend.persistence.lite.sql.postgresql.Postgresql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        Database db = Persistence.databaseBuilder("postgres", 0)
                .setDaoImplProvider(Postgresql.class)
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

        Dao<Artist> ad = db.getDaoFactory().ofEntity(Artist.class);
        System.out.println(ad.retrieve(0));
        List<Artist> artists = ad.retrieve();
        System.out.println(artists);
    }

    @Entity(name = "artist")
    private static class Artist {

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

}
