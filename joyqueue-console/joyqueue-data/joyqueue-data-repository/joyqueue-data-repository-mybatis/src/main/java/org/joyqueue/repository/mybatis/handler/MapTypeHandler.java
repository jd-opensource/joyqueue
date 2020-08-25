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
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * @author jiangnan53
 * @date 2020/6/22
 **/
public class MapTypeHandler extends BaseTypeHandler<Map<String, String>> {

    private Class<Map<String, String>> clazz;

    public MapTypeHandler(Class<Map<String, String>> clazz) {
        this.clazz = clazz;
    }

    public MapTypeHandler() {}

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map o, JdbcType jdbcType) throws SQLException {
        if (o != null) {
            preparedStatement.setString(i, JSON.toJSONString(o));
        } else {
            preparedStatement.setString(i, null);
        }
    }

    @Override
    public Map<String, String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        if (StringUtils.isNotBlank(s)) {
            return JSON.parseObject(resultSet.getString(s), clazz);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String result = resultSet.getString(i);
        if (StringUtils.isNotBlank(result)) {
            return JSON.parseObject(result, clazz);
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String result = callableStatement.getString(i);
        if (StringUtils.isNotBlank(result)) {
            return JSON.parseObject(result, clazz);
        }
        return Collections.emptyMap();
    }
}
