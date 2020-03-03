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
    public Object retrieve(Integer key) {
        return null;
    }

    @Override
    public List<Object> retrieveAll() {
        return null;
    }

    @Override
    public boolean update(Object entity) {
        return false;
    }

    @Override
    public boolean delete(Object entity) {
        return false;
    }

}
