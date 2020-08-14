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
package org.joyqueue.nsr.sql.service;

import org.joyqueue.nsr.sql.BatchOperationContext;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQLTransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/27
 */
public class SQLTransactionInternalService implements TransactionInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(SQLTransactionInternalService.class);

    @Override
    public void begin() {
        BatchOperationContext.begin();
    }

    @Override
    public void commit() {
        BatchOperationContext.commit();
    }

    @Override
    public void rollback() {
        BatchOperationContext.rollback();
    }
}