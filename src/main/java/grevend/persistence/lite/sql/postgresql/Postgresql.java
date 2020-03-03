package grevend.persistence.lite.sql.postgresql;

import grevend.persistence.lite.dao.Dao;

import java.util.Collection;
import java.util.List;

public class Postgresql implements Dao<Object> {

    @Override
    public boolean create(Collection<Object> entities) {
        return false;
    }

    @Override
    public Object retrieve(int id) {
        return null;
    }

    @Override
    public List<Object> retrieve() {
        return null;
    }

    @Override
    public boolean update(Object e) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

}
