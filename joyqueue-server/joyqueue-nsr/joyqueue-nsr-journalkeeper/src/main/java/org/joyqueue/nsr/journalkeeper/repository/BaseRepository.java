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
package org.joyqueue.nsr.journalkeeper.repository;

import org.joyqueue.nsr.journalkeeper.BatchOperationContext;
import org.joyqueue.nsr.journalkeeper.helper.ResultSetHelper;
import io.journalkeeper.sql.client.BatchSQLOperator;
import io.journalkeeper.sql.client.SQLOperator;
import io.journalkeeper.sql.client.domain.ResultSet;

import java.util.List;

/**
 * BaseRepository
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class BaseRepository {

    private SQLOperator sqlOperator;

    public BaseRepository(SQLOperator sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

    public String insert(String sql, Object... params) {
        BatchSQLOperator batchSQLOperator = BatchOperationContext.getBatchSQLOperator();
        if (batchSQLOperator != null) {
            batchSQLOperator.insert(sql, params);
            return null;
        } else {
            Object result = sqlOperator.insert(sql, params);
            if (result == null) {
                return null;
            } else {
                return result.toString();
            }
        }
    }

    public int update(String sql, Object... params) {
        BatchSQLOperator batchSQLOperator = BatchOperationContext.getBatchSQLOperator();
        if (batchSQLOperator != null) {
            batchSQLOperator.update(sql, params);
            return 0;
        } else {
            return sqlOperator.update(sql, params);
        }
    }

    public int delete(String sql, Object... params) {
        BatchSQLOperator batchSQLOperator = BatchOperationContext.getBatchSQLOperator();
        if (batchSQLOperator != null) {
            batchSQLOperator.delete(sql, params);
            return 0;
        } else {
            return sqlOperator.delete(sql, params);
        }
    }

    public ResultSet query(String sql, Object... params) {
        return sqlOperator.query(sql, params);
    }

    public int count(String sql, Object... params) {
        ResultSet resultSet = query(sql, params);
        return Integer.valueOf(resultSet.getRows().get(0).entrySet().iterator().next().getValue());
    }

    public <T> List<T> query(Class<T> type, String sql, List params) {
        Object[] array = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            array[i] = params.get(i);
        }
        return query(type, sql, array);
    }

    public <T> T queryOnce(Class<T> type, String sql, List params) {
        Object[] array = new Object[params.size()];
        for (int i = 0; i < params.size(); i++) {
            array[i] = params.get(i);
        }
        return queryOnce(type, sql, array);
    }

    public <T> List<T> query(Class<T> type, String sql, Object... params) {
        ResultSet resultSet = query(sql, params);
        return ResultSetHelper.assembleList(type, resultSet);
    }

    public <T> T queryOnce(Class<T> type, String sql, Object... params) {
        ResultSet resultSet = query(sql, params);
        return ResultSetHelper.assembleOnce(type, resultSet);
    }
}