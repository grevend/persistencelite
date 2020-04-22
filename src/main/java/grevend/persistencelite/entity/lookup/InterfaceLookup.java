/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.entity.lookup;

import grevend.persistencelite.entity.Entity;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.entity.Ignore;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of the {@code ComponentLookup} interface for interface-based entities.
 *
 * @param <E> The entity type.
 *
 * @author David Greven
 * @see ComponentLookup
 * @since 0.2.0
 */
public class InterfaceLookup<E> implements ComponentLookup<E, Method> {

    /**
     * Generates a {@code Stream} of annotated member components.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return The {@code Stream} of components.
     *
     * @see Stream
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Stream<Method> components(@NotNull EntityMetadata<E> entityMetadata) {
        return Stream.of(entityMetadata.getEntityClass().getDeclaredMethods()).filter(
            method -> method.getParameterCount() == 0 && !method.isDefault() && !method.isBridge()
                && !method.isVarArgs() && !method.isSynthetic() && !Modifier
                .isStatic(method.getModifiers()) && !method.isAnnotationPresent(Ignore.class));
    }

    /**
     * Looks up the type of the annotated member component.
     *
     * @param component The component from which the type is to be looked up.
     *
     * @return The component type in form of a {@code Class}.
     *
     * @see Class
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Class<?> lookupComponentType(@NotNull Method component) {
        return component.getReturnType();
    }

    /**
     * Looks up the name of the annotated member component.
     *
     * @param component The component from which the name is to be looked up.
     *
     * @return The component name.
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    public String lookupComponentName(@NotNull Method component) {
        return component.getName();
    }

    /**
     * Creates a {@code MethodHandle} for the passed component.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     * @param lookup         The factory used to create the getter {@code MethodHandle}.
     * @param component      The component for which the getter should be looked up.
     *
     * @return Returns either the getter's MethodHandle or null.
     *
     * @see MethodHandle
     * @see EntityMetadata
     * @see ComponentLookup
     * @since 0.2.0
     */
    @Nullable
    @Override
    public MethodHandle lookupGetter(@NotNull EntityMetadata<E> entityMetadata, @NotNull MethodHandles.Lookup lookup, @NotNull Method component) {
        try {
            return lookup.unreflect(component);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create a {@code MethodHandle} for the entity constructor.
     *
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return Returns either the constructor's MethodHandle or null.
     *
     * @see MethodHandle
     * @see EntityMetadata
     * @since 0.2.0
     */
    @Nullable
    @Override
    public MethodHandle lookupConstructor(@NotNull EntityMetadata<E> entityMetadata) {
        return null;
    }

    /**
     * @param entityMetadata The metadata of the entity for which this operation is being
     *                       performed.
     *
     * @return Returns a collection of all the super types.
     *
     * @see Collection
     * @see EntityMetadata
     * @since 0.2.0
     */
    @NotNull
    @Override
    public Collection<EntityMetadata<?>> lookupSuperTypes(@NotNull EntityMetadata<E> entityMetadata) {
        return Stream.of(entityMetadata.getEntityClass().getInterfaces())
            .filter(superType -> superType.isAnnotationPresent(Entity.class))
            .map(EntityMetadata::of)
            .collect(Collectors.toList());
    }

}
