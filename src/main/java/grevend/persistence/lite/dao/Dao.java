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

package grevend.persistence.lite.dao;

import grevend.persistence.lite.database.Database;
import grevend.persistence.lite.util.Tuple;
import grevend.persistence.lite.util.sequence.LazySeq;
import grevend.persistence.lite.util.sequence.Seq;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public interface Dao<E> {

  boolean create(@NotNull E entity);

  default boolean createAll(@NotNull Collection<E> entities) {
    return entities.stream().allMatch(this::create);
  }

  Optional<E> retrieveByKey(@NotNull Tuple key);

  Collection<E> retrieveByAttributes(@NotNull Map<String, ?> attributes);

  @NotNull Collection<E> retrieveAll();

  default @NotNull Stream<E> stream() {
    return this.retrieveAll().stream();
  }

  default @NotNull Stream<E> parallelStream() {
    return this.retrieveAll().parallelStream();
  }

  default @NotNull Seq<E> sequence() {
    return LazySeq.of(this.retrieveAll());
  }

  default boolean update(@NotNull E entity) {
    return this.delete(entity) && this.create(entity);
  }

  default boolean updateAll(@NotNull Collection<E> entities) {
    return entities.stream().allMatch(this::update);
  }

  boolean delete(@NotNull E entity);

  boolean deleteByKey(@NotNull Tuple key);

  boolean deleteByAttributes(@NotNull Map<String, ?> attributes);

  default boolean deleteAll(@NotNull Collection<E> entities) {
    return entities.stream().allMatch(this::delete);
  }

  @NotNull Database getDatabase();

}
