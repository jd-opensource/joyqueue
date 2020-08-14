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
package org.joyqueue.nsr.journalkeeper;

import io.journalkeeper.sql.client.SQLClient;
import io.journalkeeper.sql.server.SQLServer;
import org.joyqueue.monitor.PointTracer;
import org.joyqueue.nsr.journalkeeper.repository.JournalkeeperBaseRepository;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperClusterInternalService;
import org.joyqueue.nsr.journalkeeper.service.JournalkeeperTransactionInternalService;
import org.joyqueue.nsr.service.internal.ClusterInternalService;
import org.joyqueue.nsr.service.internal.TransactionInternalService;
import org.joyqueue.nsr.sql.SQLInternalServiceManager;
import org.joyqueue.nsr.sql.operator.SQLOperator;
import org.joyqueue.nsr.sql.repository.BaseRepository;

/**
 * JournalkeeperInternalServiceManager
 * author: gaohaoxiang
 * date: 2019/8/15
 */
public class JournalkeeperInternalServiceManager extends SQLInternalServiceManager {

    private SQLServer sqlServer;
    private SQLClient sqlClient;
    private SQLOperator sqlOperator;

    private JournalkeeperClusterInternalService journalkeeperClusterInternalService;
    private JournalkeeperTransactionInternalService journalkeeperTransactionInternalService;

    public JournalkeeperInternalServiceManager(SQLServer sqlServer, SQLClient sqlClient, SQLOperator sqlOperator, PointTracer tracer) {
        super(sqlOperator, tracer);
        this.sqlServer = sqlServer;
        this.sqlClient = sqlClient;
        this.sqlOperator = sqlOperator;
    }

    @Override
    protected void validate() throws Exception {
        super.validate();
        journalkeeperClusterInternalService = new JournalkeeperClusterInternalService(sqlClient);
        journalkeeperTransactionInternalService = new JournalkeeperTransactionInternalService();
    }

    @Override
    protected BaseRepository createBaseRepository(SQLOperator sqlOperator, PointTracer tracer) {
        return new JournalkeeperBaseRepository(sqlOperator, tracer);
    }

    @Override
    public <T> T getService(Class<T> service) {
        if (service.equals(TransactionInternalService.class)) {
            return (T) journalkeeperTransactionInternalService;
        } else if (service.equals(ClusterInternalService.class)) {
            return (T) journalkeeperClusterInternalService;
        } else {
            return super.getService(service);
        }
    }
}