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
package org.joyqueue.nsr.sql.repository;

import org.joyqueue.monitor.PointTracer;
import org.joyqueue.monitor.TraceStat;
import org.joyqueue.nsr.sql.BatchOperationContext;
import org.joyqueue.nsr.sql.helper.ResultSetHelper;
import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.operator.ResultSet;
import org.joyqueue.nsr.sql.operator.SQLOperator;

import java.util.List;

/**
 * BaseRepository
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class BaseRepository {

    private SQLOperator sqlOperator;
    private PointTracer tracer;

    public BaseRepository(SQLOperator sqlOperator, PointTracer tracer) {
        this.sqlOperator = sqlOperator;
        this.tracer = tracer;
    }

    public String insert(String sql, Object... params) {
        TraceStat trace = tracer.begin(getTraceKey(sql));
        try {
            String result = doInsert(sql, params);
            tracer.end(trace);
            return result;
        } catch (Exception e) {
            tracer.error(trace);
            throw e;
        }
    }

    protected String doInsert(String sql, Object... params) {
        BatchSQLOperator batchSQLOperator = getBatchSQLOperator();
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
        TraceStat trace = tracer.begin(getTraceKey(sql));
        try {
            int result = doUpdate(sql, params);
            tracer.end(trace);
            return result;
        } catch (Exception e) {
            tracer.error(trace);
            throw e;
        }
    }

    protected int doUpdate(String sql, Object... params) {
        BatchSQLOperator batchSQLOperator = getBatchSQLOperator();
        if (batchSQLOperator != null) {
            batchSQLOperator.update(sql, params);
            return 0;
        } else {
            return sqlOperator.update(sql, params);
        }
    }

    public int delete(String sql, Object... params) {
        TraceStat trace = tracer.begin(getTraceKey(sql));
        try {
            int result = doDelete(sql, params);
            tracer.end(trace);
            return result;
        } catch (Exception e) {
            tracer.error(trace);
            throw e;
        }
    }

    protected int doDelete(String sql, Object... params) {
        BatchSQLOperator batchSQLOperator = getBatchSQLOperator();
        if (batchSQLOperator != null) {
            batchSQLOperator.delete(sql, params);
            return 0;
        } else {
            return sqlOperator.delete(sql, params);
        }
    }

    public ResultSet query(String sql, Object... params) {
        TraceStat trace = tracer.begin(getTraceKey(sql));
        try {
            ResultSet result = doQuery(sql, params);
            tracer.end(trace);
            return result;
        } catch (Exception e) {
            tracer.error(trace);
            throw e;
        }
    }

    protected ResultSet doQuery(String sql, Object... params) {
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

    protected BatchSQLOperator getBatchSQLOperator() {
        return BatchOperationContext.getBatchSQLOperator();
    }

    protected String getTraceKey(String name) {
        return "NameService." + sqlOperator.getClass().getSimpleName() + name.replace(" = ?", "_")
                .replace(" ", "_")
                .replace("`", "")
                .replace(",", "");
    }
}