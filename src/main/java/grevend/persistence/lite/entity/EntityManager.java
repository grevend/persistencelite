package grevend.persistence.lite.entity;

import grevend.persistence.lite.util.Ignore;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class EntityManager {

    private static EntityManager instance;

    private EntityManager() {
    }

    public static synchronized @NotNull EntityManager getInstance() {
        if (instance == null) {
            instance = new EntityManager();
        }
        return instance;
    }

    private boolean hasViableConstructor(@NotNull Class<?> entity) {
        return Arrays.stream(entity.getDeclaredConstructors())
                .anyMatch(constructor -> constructor.getParameterCount() == 0
                        && !constructor.isSynthetic()
                        && (Modifier.isPublic(constructor.getModifiers())
                        || Modifier.isProtected(constructor.getModifiers())));
    }

    private @NotNull List<Field> getAllViableFields(@NotNull Class<?> entity) {
        return Arrays.stream(entity.getDeclaredFields()).filter(field -> (!field.isSynthetic()
                && !field.isAnnotationPresent(Ignore.class)
                && !Modifier.isAbstract(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !Modifier.isStatic(field.getModifiers())))
                .collect(Collectors.toList());
    }

    private @NotNull String getAttributeName(@NotNull Field field) {
        return field.isAnnotationPresent(Attribute.class)
                ? field.getAnnotation(Attribute.class).name() : field.getName();
    }

    private @NotNull List<Field> getFieldsWithAnnotation(
            @NotNull List<Field> fields, @NotNull Class<? extends Annotation> annotation) {
        return fields.stream().filter(field -> field.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    private @NotNull Optional<Constructor<?>> getConstructor(@NotNull Class<?> entity) {
        if (this.hasViableConstructor(entity)) {
            List<Constructor<?>> constructors = Arrays.stream(entity.getDeclaredConstructors())
                    .filter(constructor -> constructor.getParameterCount() == 0
                            && !constructor.isSynthetic()
                            && (Modifier.isPublic(constructor.getModifiers())
                            || Modifier.isProtected(constructor.getModifiers())))
                    .collect(Collectors.toList());
            return Optional.ofNullable(constructors.size() > 0 ? constructors.get(0) : null);
        } else {
            return Optional.empty();
        }
    }

    private @NotNull <A> A constructEntity(@NotNull Class<A> entity)
            throws IllegalStateException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, InstantiationException {
        Optional<Constructor<?>> constructor = getConstructor(entity);
        if (constructor.isPresent()) {
            constructor.get().setAccessible(true);
            return entity.cast(constructor.get().newInstance());
        } else {
            throw new IllegalArgumentException("Class " + entity.getCanonicalName()
                    + " must declare an empty public or protected constructor");
        }
    }

    public @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull Map<String, Object> values)
            throws IllegalStateException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, InstantiationException, NoSuchFieldException {
        A obj = constructEntity(entity);
        /*for (Map.Entry<String, Pair<String, Class<?>>> row : this.entities.get(entity).getAttributes().entrySet()) {
            if (values.containsKey(row.getKey())) {
                Field field = entity.getField(row.getValue().getA());
                boolean isAccessible = field.canAccess(obj);
                field.setAccessible(true);
                field.set(obj, values.get(row.getKey()));
                field.setAccessible(isAccessible);
            } else {
                throw new IllegalArgumentException("The values do not contain a value for the row " + row);
            }
        }*/
        return obj;
    }

    public @NotNull <A> A constructEntity(@NotNull Class<A> entity, @NotNull ResultSet resultSet) throws SQLException,
            IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        A obj = constructEntity(entity);
        ResultSetMetaData metaData = resultSet.getMetaData();
        /*if (this.entities.get(entity).getAttributes().entrySet().size() == metaData.getColumnCount()) {
            for (Map.Entry<String, Pair<String, Class<?>>> row : this.entities.get(entity).getAttributes().entrySet()) {
                for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
                    if (row.getKey().equals(metaData.getColumnName(i))) {
                        Field field = entity.getField(row.getValue().getA());
                        boolean isAccessible = field.canAccess(obj);
                        field.setAccessible(true);
                        field.set(obj, resultSet.getObject(i));
                        field.setAccessible(isAccessible);
                    }
                }
            }
        }*/
        return obj;
    }

}
