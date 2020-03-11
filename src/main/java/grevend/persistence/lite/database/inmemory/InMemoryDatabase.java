package grevend.persistence.lite.database.inmemory;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.entity.EntityClass;
import grevend.persistence.lite.util.Pair;
import grevend.persistence.lite.util.Triplet;
import grevend.persistence.lite.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InMemoryDatabase extends Database {

    private static Set<Class<?>> primitives = Set.of(
            Void.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
            Float.TYPE, Double.TYPE, Boolean.TYPE, Character.TYPE);
    private Map<EntityClass<?>, List<Object>> storage;

    public InMemoryDatabase(@NotNull String name, int version, @NotNull String user, @NotNull String password) {
        super(name, version, user, password);
        this.storage = new HashMap<>();
    }

    @Override
    public @NotNull URI getURI() throws URISyntaxException {
        return new File(this.getName() + ".ser").toURI();
    }

    public Map<EntityClass<?>, List<Object>> getStorage() {
        return this.storage;
    }

    private <A> boolean checkKey(@NotNull A entity, @NotNull Map<String, ?> keyValuePairs,
                                @NotNull Collection<Triplet<Class<?>, String, String>> keys) {
        return keys.stream().allMatch(k -> {
            try {
                Field field = entity.getClass().getField(k.getB());
                field.setAccessible(true);
                return Objects.equals(field.get(entity), keyValuePairs.get(k.getB()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return false;
            }
        });
    }

    @Override
    public @NotNull <A> Dao<A> createDao(@NotNull EntityClass<A> entityClass,
                                         @NotNull List<Triplet<Class<?>, String, String>> keys) {
        if (!Serializable.class.isAssignableFrom(entityClass.getEntityClass())) {
            throw new IllegalArgumentException("InMemoryDaoFactory only supports entities that implement the " +
                    Serializable.class.getCanonicalName() + " interface.");
        }
        if (!keys.stream()
                .allMatch(key -> Serializable.class.isAssignableFrom(key.getA()) || primitives.contains(key.getA()))) {
            throw new IllegalArgumentException(
                    "InMemoryDaoFactory only supports entities with keys that implement the " +
                            Serializable.class.getCanonicalName() + " interface.");
        }
        if (!this.storage.containsKey(entityClass)) {
            this.storage.put(entityClass, new ArrayList<>());
        }
        return new Dao<>() {

            @Override
            public boolean create(@NotNull A entity) {
                if (!InMemoryDatabase.this.storage.get(entityClass).contains(entity)) {
                    InMemoryDatabase.this.storage.get(entityClass).add(entity);
                    return true;
                }
                return false;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Optional<A> retrieveByKey(@NotNull Tuple key) {
                Map<?, ?> attributes = IntStream.range(0, key.count())
                        .mapToObj(i -> Pair
                                .of((String & Serializable) keys.get(i).getB(),
                                        (Serializable) key.get(i, keys.get(i).getA())))
                        .collect(Pair.toMap());
                Collection<A> res = this.retrieveByAttributes((Map<String, ?>) attributes);
                if (res != null) {
                    return res.size() != 1 ? Optional.empty() : Optional.ofNullable(res.iterator().next());
                } else {
                    return Optional.empty();
                }
            }

            @Override
            public Collection<A> retrieveByAttributes(@NotNull Map<String, ?> attributes) {
                return this
                        .retrieveAll().stream().filter(entity -> InMemoryDatabase.this.checkKey(entity, attributes, keys))
                        .collect(Collectors.toList());
            }

            @Override
            @SuppressWarnings("unchecked")
            public @NotNull Collection<A> retrieveAll() {
                return (Collection<A>) InMemoryDatabase.this.storage.get(entityClass);
            }

            @Override
            public boolean delete(@NotNull A entity) {
                if (InMemoryDatabase.this.storage.containsKey(entityClass)) {
                    InMemoryDatabase.this.storage.get(entityClass).remove(entity);
                    return true;
                }
                return false;
            }

            @Override
            public boolean deleteByKey(@NotNull Tuple key) {
                return false;
            }

        };
    }

}
