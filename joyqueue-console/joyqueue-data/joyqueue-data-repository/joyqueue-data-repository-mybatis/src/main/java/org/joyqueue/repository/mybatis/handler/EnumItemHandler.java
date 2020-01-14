/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.repository.mybatis.handler;

import org.joyqueue.model.domain.EnumItem;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by gouyaoqing on 2017/3/23.
 */
public class EnumItemHandler<E extends EnumItem> extends BaseTypeHandler<E> {
    private Class<E> type;
    private E[] enums;

    public EnumItemHandler(Class<E> type){
        if(type == null){
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
        this.enums = type.getEnumConstants();
        if (this.enums == null)
            throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
    }
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, E enumItem, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, enumItem.value());
    }

    @Override
    public E getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        int i = resultSet.getInt(columnName);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return locateEnumStatus(i);
        }
    }

    @Override
    public E getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int result = resultSet.getInt(i);
        if (resultSet.wasNull()) {
            return null;
        } else {
            return locateEnumStatus(result);
        }
    }

    @Override
    public E getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int result = callableStatement.getInt(i);
        if (callableStatement.wasNull()) {
            return null;
        } else {
            return locateEnumStatus(result);
        }
    }

    private E locateEnumStatus(int value) {
        for(E e : enums) {
            if(e.value() == value) {
                return e;
            }
        }
        return null;
    }
}
