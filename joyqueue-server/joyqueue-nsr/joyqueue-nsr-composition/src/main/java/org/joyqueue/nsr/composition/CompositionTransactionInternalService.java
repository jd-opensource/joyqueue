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
package org.joyqueue.nsr.composition;

import org.joyqueue.nsr.composition.config.CompositionConfig;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CompositionTransactionInternalService
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class CompositionTransactionInternalService implements TransactionInternalService {

    protected static final Logger logger = LoggerFactory.getLogger(CompositionTransactionInternalService.class);

    private CompositionConfig config;
    private TransactionInternalService sourceTransactionInternalService;
    private TransactionInternalService targetTransactionInternalService;

    public CompositionTransactionInternalService(CompositionConfig config, TransactionInternalService sourceTransactionInternalService,
                                                 TransactionInternalService targetTransactionInternalService) {
        this.config = config;
        this.sourceTransactionInternalService = sourceTransactionInternalService;
        this.targetTransactionInternalService = targetTransactionInternalService;
    }

    @Override
    public void begin() {
        if (config.isWriteSource()) {
            sourceTransactionInternalService.begin();
        }
        if (config.isWriteTarget()) {
            try {
                targetTransactionInternalService.begin();
            } catch (Exception e) {
                logger.info("transaction begin exception", e);
            }
        }
    }

    @Override
    public void commit() {
        if (config.isWriteSource()) {
            sourceTransactionInternalService.commit();
        }
        if (config.isWriteTarget()) {
            try {
                targetTransactionInternalService.commit();
            } catch (Exception e) {
                logger.info("transaction commit exception", e);
            }
        }
    }

    @Override
    public void rollback() {
        if (config.isWriteSource()) {
            sourceTransactionInternalService.rollback();
        }
        if (config.isWriteTarget()) {
            try {
                targetTransactionInternalService.rollback();
            } catch (Exception e) {
                logger.info("transaction rollback exception", e);
            }
        }
    }
}