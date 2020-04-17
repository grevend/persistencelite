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

package grevend.persistencelite.service.sql;

import grevend.persistencelite.dao.BaseDao;
import grevend.persistencelite.entity.EntityMetadata;
import grevend.persistencelite.service.sql.PreparedStatementFactory.StatementType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SqlDao<E> extends BaseDao<E, SqlTransaction> {

    private final PreparedStatementFactory preparedStatementFactory;

    public SqlDao(@NotNull EntityMetadata<E> entityMetadata, @Nullable SqlTransaction transaction) {
        super(entityMetadata, transaction);
        this.preparedStatementFactory = new PreparedStatementFactory();
    }

    @NotNull
    @Override
    @Contract("_, _ -> param1")
    protected E create(@NotNull E entity, @NotNull Collection<Map<String, Object>> properties) throws SQLException {
        var iterator = this.getEntityMetadata().getSuperTypes().iterator();
        //System.out.println(this.getEntityMetadata().getSuperTypes().size() + " :: " + properties.size());
        for (Map<String, Object> props : properties) {
            this.create(iterator.hasNext() ? iterator.next() : this.getEntityMetadata(), props);
        }
        return entity;
    }

    private void create(@NotNull EntityMetadata<?> entityMetadata, @NotNull Map<String, Object> properties) throws SQLException {
        var preparedStatement = this.preparedStatementFactory.prepare(StatementType.INSERT,
            Objects.requireNonNull(this.getTransaction()).connection(), entityMetadata,
            properties);
        this.setStatementValues(entityMetadata, preparedStatement, properties);
        //System.out.println(entityMetadata.getName() + " ;; " + properties);
        preparedStatement.executeUpdate();
    }

    private void setStatementValues(@NotNull EntityMetadata<?> entityMetadata, @NotNull PreparedStatement statement, @NotNull Map<String, Object> properties) throws SQLException {
        var i = 0;
        //System.out.println("Values to set: " + entityMetadata.getName() + " ;; " + properties);
        for (Entry<String, Object> attribute : properties.entrySet()) {
            if (attribute.getValue() == null || attribute.getValue().equals("null")) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, attribute.getValue());
            }
            i++;
        }
    }

}
