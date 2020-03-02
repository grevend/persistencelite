package grevend.persistence.lite.dao;

import java.util.Collection;
import java.util.List;

public interface Dao<T> {

    boolean create(Collection<T> entities);

    T retrieve(int id);

    List<T> retrieve();

    boolean update(T e);

    boolean delete(int id);

}
