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
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author David Greven
 * @since 0.4.7
 */
public final class RestDaoImpl implements DaoImpl<IOException> {

    private final EntityMetadata<?> entityMetadata;
    private final EntitySerializer<Reader> entitySerializer;
    private final EntityDeserializer<String> entityDeserializer;

    @Contract(pure = true)
    public RestDaoImpl(@NotNull EntityMetadata<?> entityMetadata) {
        this.entityMetadata = entityMetadata;
        this.entitySerializer = json -> List.of(new Gson().fromJson(json, Map.class));
        this.entityDeserializer = entity -> "";
    }

    @Override
    public void create(@NotNull Iterable<Map<String, Object>> entity) throws IOException {

    }

    @NotNull
    @Contract(" -> new")
    private Reader request() throws IOException {
        var con = new URL("http://localhost:8000/api/v2/country").openConnection();
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("Content-Type", "application/pl.v0.entity+json; utf-8");
        return new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
    }

    @NotNull
    @Override
    public Iterable<Map<String, Object>> retrieve(@NotNull Iterable<String> keys, @NotNull Map<String, Object> props) throws IOException {
        try {
            /*var reader = new BufferedReader(this.request());

            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }*/

            var response = new Gson().fromJson(this.request(), EntityRequestResponse.class);

            //response.entities.stream().map(entity -> entity.props).collect(Collectors.toUnmodifiableList());

            //System.out.println();

            var types = Map.<Class<?>, Function<String, ?>>of(
                Integer.TYPE, Integer::valueOf,
                Integer.class, Integer::valueOf,
                String.class, s -> s
            );

            //return this.entitySerializer.serialize(this.request());
            //return this.entitySerializer.serialize(new StringReader("{}"));
            return response.entities.stream().map(
                entity -> this.entityMetadata.uniqueProperties().stream().map(
                    prop -> new SimpleEntry<String, Object>(prop.fieldName(), types.get(prop.type())
                        .apply(entity.props.containsKey(prop.fieldName()) ? entity.props
                            .get(prop.fieldName())
                            : (entity.props.getOrDefault(prop.propertyName(), null)))))
                    .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue,
                        (olvV, newV) -> newV)))
                .collect(Collectors.toUnmodifiableList());
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

    public static class EntityRequestResponse {

        public Map<String, String> types;
        public Collection<ResponseEntity> entities;

        @Override
        public String toString() {
            return "EntityRequestResponse{" +
                "types=" + types +
                ", entities=" + entities +
                '}';
        }
    }

    public static class ResponseEntity {

        public int type;
        public Map<String, String> props;
        public Map<String, String> rels;

        @Override
        public String toString() {
            return "ResponseEntity{" +
                "type=" + type +
                ", props=" + props +
                ", rels=" + rels +
                '}';
        }
    }

}
