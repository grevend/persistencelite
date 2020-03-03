package grevend.persistence.lite.dao;

import java.util.Collection;
import java.util.List;

public interface Dao<T, K> {

    boolean create(Collection<T> entities);

    T retrieve(K id);

    List<T> retrieveAll();

    boolean update(T e);

    boolean delete(K id);

}
