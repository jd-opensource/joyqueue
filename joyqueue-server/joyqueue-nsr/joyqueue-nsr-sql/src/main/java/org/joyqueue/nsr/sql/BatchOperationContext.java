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
package org.joyqueue.nsr.sql;

import org.joyqueue.nsr.sql.operator.BatchSQLOperator;
import org.joyqueue.nsr.sql.operator.SQLOperator;

/**
 * BatchOperationContext
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class BatchOperationContext {

    private static final ThreadLocal<BatchSQLOperator> batchOperatorThreadLocal = new ThreadLocal<>();

    private static SQLOperator sqlOperator;

    public static void init(SQLOperator sqlOperator) {
        if (BatchOperationContext.sqlOperator != null) {
            return;
        }
        BatchOperationContext.sqlOperator = sqlOperator;
    }

    public static void begin() {
        BatchSQLOperator batchSQLOperator = sqlOperator.beginBatch();
        batchOperatorThreadLocal.set(batchSQLOperator);
    }

    public static void commit() {
        BatchSQLOperator batchSQLOperator = batchOperatorThreadLocal.get();
        if (batchSQLOperator == null) {
            throw new UnsupportedOperationException("batch not exist");
        }
        batchSQLOperator.commit();
        batchOperatorThreadLocal.remove();
    }

    public static void rollback() {
        BatchSQLOperator batchSQLOperator = batchOperatorThreadLocal.get();
        if (batchSQLOperator == null) {
            throw new UnsupportedOperationException("batch not exist");
        }
        batchSQLOperator.rollback();
        batchOperatorThreadLocal.remove();
    }

    public static void close() {
        BatchSQLOperator batchSQLOperator = batchOperatorThreadLocal.get();
        if (batchSQLOperator == null) {
            throw new UnsupportedOperationException("batch not exist");
        }
        batchOperatorThreadLocal.remove();
    }

    public static BatchSQLOperator getBatchSQLOperator() {
        return batchOperatorThreadLocal.get();
    }
}