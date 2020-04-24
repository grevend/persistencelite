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

package grevend.persistencelite.entity;

import grevend.jacoco.Generated;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @see Relation
 * @see EntityRelationType
 * @see EntityProperty
 * @since 0.2.0
 */
public class EntityRelation {

    private final String[] selfProperties, targetProperties;
    private final Class<?> targetEntity;
    private EntityRelationType type;
    private boolean circularDependency;

    /**
     * @param selfProperties
     * @param targetEntity
     * @param targetProperties
     * @param type
     * @param circularDependency
     *
     * @since 0.2.0
     */
    public EntityRelation(@NotNull String[] selfProperties, @NotNull Class<?> targetEntity, @NotNull String[] targetProperties, @NotNull EntityRelationType type, boolean circularDependency) {
        this.selfProperties = selfProperties;
        this.targetEntity = targetEntity;
        this.targetProperties = targetProperties;
        this.type = type;
        this.circularDependency = circularDependency;

        if (selfProperties.length != targetProperties.length) {
            throw new IllegalArgumentException(
                Arrays.toString(selfProperties) + " should contain the same amount of elements as "
                    + Arrays.toString(targetProperties) + ".");
        }
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Generated
    public String[] getSelfProperties() {
        return this.selfProperties;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Generated
    public String[] getTargetProperties() {
        return this.targetProperties;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Generated
    public Class<?> getTargetEntity() {
        return this.targetEntity;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Generated
    public EntityRelationType getType() {
        return this.type;
    }

    /**
     * @param type
     *
     * @since 0.2.0
     */
    @Generated
    public void setType(@NotNull EntityRelationType type) {
        this.type = type;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @Generated
    public boolean isCircularDependency() {
        return this.circularDependency;
    }

    /**
     * @param circularDependency
     *
     * @since 0.2.0
     */
    @Generated
    public void setCircularDependency(boolean circularDependency) {
        this.circularDependency = circularDependency;
    }

    /**
     * @param o
     *
     * @return
     *
     * @since 0.2.0
     */
    @Override
    @Generated
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || this.getClass() != o.getClass()) { return false; }
        EntityRelation that = (EntityRelation) o;
        return this.isCircularDependency() == that.isCircularDependency() &&
            Arrays.equals(this.getSelfProperties(), that.getSelfProperties()) &&
            Arrays.equals(this.getTargetProperties(), that.getTargetProperties()) &&
            this.getTargetEntity().equals(that.getTargetEntity()) &&
            this.getType() == that.getType();
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @Override
    @Generated
    public int hashCode() {
        int result = Objects
            .hash(this.getTargetEntity(), this.getType(), this.isCircularDependency());
        result = 31 * result + Arrays.hashCode(this.getSelfProperties());
        result = 31 * result + Arrays.hashCode(this.getTargetProperties());
        return result;
    }

    /**
     * @return
     *
     * @since 0.2.0
     */
    @NotNull
    @Override
    @Generated
    public String toString() {
        return "EntityRelation{" +
            "selfProperties=" + Arrays.toString(this.selfProperties) +
            ", targetProperties=" + Arrays.toString(this.targetProperties) +
            ", targetEntity=" + this.targetEntity +
            ", type=" + this.type +
            ", circularDependency=" + this.circularDependency +
            '}';
    }

}
