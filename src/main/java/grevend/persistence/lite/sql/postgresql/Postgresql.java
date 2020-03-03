package grevend.persistence.lite.sql.postgresql;

import grevend.persistence.lite.dao.Dao;

import java.util.Collection;
import java.util.List;

public class Postgresql implements Dao<Object, Integer> {

    @Override
    public boolean create(Collection<Object> entities) {
        return false;
    }

    @Override
    public Object retrieve(Integer id) {
        return null;
    }

    @Override
    public List<Object> retrieveAll() {
        return null;
    }

    @Override
    public boolean update(Object e) {
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        return false;
    }

}
