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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * ListTypeHandler
 * Created by chenyanying3 on 2018-12-12.
 */
public class ListTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private Class<T> type;

    public ListTypeHandler(Class<T> type){
        if(type == null){
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, null);
        } else {
            ps.setString(i, JSON.toJSONString(parameter));
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (rs==null || rs.wasNull()) {
            return null;
        }
        String columnValue = rs.getString(columnName);
        if (StringUtils.isBlank(columnValue)) {
            return null;
        }

        return JSONArray.parseArray(columnValue, type);
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (rs==null || rs.wasNull()) {
            return null;
        }
        String columnValue = rs.getString(columnIndex);
        if (StringUtils.isBlank(columnValue)) {
            return null;
        }
        return JSONArray.parseArray(columnValue, type);
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (cs==null || cs.wasNull()) {
            return null;
        }
        String columnValue = cs.getString(columnIndex);
        if (StringUtils.isBlank(columnValue)) {
            return null;
        }
        return JSONArray.parseArray(columnValue, type);

    }
}
