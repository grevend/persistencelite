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

package grevend.persistencelite;

import grevend.persistencelite.database.Database;
import grevend.persistencelite.database.DatabaseBuilder;
import grevend.persistencelite.database.DatabaseBuilderException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.NotNull;

public final class Persistence {

  public static @NotNull <B extends DatabaseBuilder<D>, D extends Database> B databaseBuilder(
      @NotNull Class<B> databaseBuilder, @NotNull String name, int version) {
    try {
      Constructor<B> constructor = databaseBuilder
          .getConstructor(String.class, Integer.TYPE);
      return constructor.newInstance(name, version);
    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
      throw new DatabaseBuilderException("DatabaseBuilder construction failed.", e);
    }
  }

}
