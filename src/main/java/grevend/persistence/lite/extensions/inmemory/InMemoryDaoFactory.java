package grevend.persistence.lite.extensions.inmemory;

import grevend.persistence.lite.dao.Dao;
import grevend.persistence.lite.dao.DaoFactory;
import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.util.Triplet;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryDaoFactory extends DaoFactory {

    private Map<Class<?>, Set<Object>> storage;

    public InMemoryDaoFactory(@NotNull Database database) {
        super(database);
        this.storage = new HashMap<>();
    }

    public <A> boolean checkKey(@NotNull A entity, @NotNull Map<String, ?> keyValuePairs,
                                @NotNull List<Triplet<Class<?>, String, String>> keys) {
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

    @SuppressWarnings("unchecked")
    public <A, B> Map<String, B> getKeyValues(@NotNull A entity,
                                              @NotNull List<Triplet<Class<?>, String, String>> keys) {
        Map<String, B> values = new HashMap<>();
        keys.forEach(k -> {
            try {
                Field field = entity.getClass().getField(k.getB());
                field.setAccessible(true);
                values.put(k.getB(), (B) field.get(entity));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <A, B> Dao<A, B> createDao(@NotNull Class<A> entity, @NotNull Class<B> keyClass,
                                               List<Triplet<Class<?>, String, String>> keys)
            throws IllegalArgumentException {
        if (!Serializable.class.isAssignableFrom(entity)) {
            throw new IllegalArgumentException("InMemoryDaoFactory only supports entities that implement the " +
                    Serializable.class.getCanonicalName() + " interface.");
        }
        if (!storage.containsKey(entity)) {
            storage.put(entity, new HashSet<>());
        }
        return new Dao<>() {

            @Override
            public boolean createAll(@NotNull Collection<A> entities) {
                storage.get(entity).addAll(entities);
                return true;
            }

            @Override
            public Optional<A> retrieve(@NotNull B key) {
                return keys.size() != 1 ? Optional.empty() : retrieve(Map.of(keys.get(0).getB(), key));
            }

            @Override
            public Optional<A> retrieve(@NotNull Map<String, ?> keyValuePairs) {
                List<?> results = storage.get(entity).stream().filter(e -> checkKey(e, keyValuePairs, keys))
                        .collect(Collectors.toList());
                return results.size() != 1 ? Optional.empty() : (Optional<A>) Optional.ofNullable(results.get(0));
            }

            @Override
            public @NotNull Set<A> retrieveAll() {
                return (Set<A>) storage.get(entity);
            }

            @Override
            public boolean updateAll(@NotNull Collection<A> entities) {
                entities.forEach(e -> {
                    Optional<A> res = retrieve(getKeyValues(e, keys));
                    if (res.isPresent()) {
                        storage.get(entity).remove(res.get());
                        storage.get(entity).add(e);
                    } else {
                        create(e);
                    }
                });
                return true;
            }

            @Override
            public boolean deleteAll(@NotNull Collection<A> entities) {
                return storage.get(entity).removeAll(entities);
            }

        };
    }

}
