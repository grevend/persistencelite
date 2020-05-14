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

package grevend.persistencelite.internal.service.rest;

import com.google.gson.Gson;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.internal.dao.DaoImpl;
import grevend.persistencelite.internal.entity.representation.EntityDeserializer;
import grevend.persistencelite.internal.entity.representation.EntitySerializer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RestRequesterDao implements DaoImpl<IOException> {

    private final EntityMetadata<?> entityMetadata;
    private final EntitySerializer<Reader> entitySerializer;
    private final EntityDeserializer<String> entityDeserializer;

    @Contract(pure = true)
    public RestRequesterDao(@NotNull EntityMetadata<?> entityMetadata) {
        this.entityMetadata = entityMetadata;
        this.entitySerializer = json -> List.of(new Gson().fromJson(json, Map.class));
        this.entityDeserializer = entity -> "";
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws IOException {

    }

    @Contract(" -> new")
    private @NotNull Reader request() throws IOException {
        var con = new URL("http://localhost:8000/api/v2/author").openConnection();
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        return new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
    }

    @NotNull
    @Override
    public Iterable<Map<String, Object>> retrieve(@NotNull Iterable<String> keys, @NotNull Map<String, Object> props) throws IOException {
        try {
            return this.entitySerializer.serialize(this.request());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return List.of();
        }
    }

    @Override
    public void update(@NotNull Iterable<Map<String, Object>> entity, @NotNull Map<String, Object> props) throws IOException {

    }

    @Override
    public void delete(@NotNull Map<String, Object> props) throws IOException {

    }

}
