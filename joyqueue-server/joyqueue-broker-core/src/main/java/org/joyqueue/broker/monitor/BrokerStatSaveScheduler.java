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
package org.joyqueue.broker.monitor;

import org.joyqueue.broker.monitor.config.BrokerMonitorConfig;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * brokerstat保存调度
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class BrokerStatSaveScheduler extends Service implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(BrokerStatSaveScheduler.class);

    private BrokerMonitorConfig config;
    private BrokerStatManager brokerStatManager;
    private ScheduledExecutorService executorService;

    public BrokerStatSaveScheduler(BrokerMonitorConfig config, BrokerStatManager brokerStatManager) {
        this.config = config;
        this.brokerStatManager = brokerStatManager;
        this.executorService = newScheduledExecutorService();
    }

    @Override
    protected void doStart() throws Exception {
        this.executorService.scheduleWithFixedDelay(this, config.getStatSaveInterval(), config.getStatSaveInterval(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doStop() {
        this.executorService.shutdown();
    }

    @Override
    public void run() {
        try {
            brokerStatManager.save();
        } catch (Exception e) {
            logger.error("save broker stat exception", e);
        }
    }

    protected ScheduledExecutorService newScheduledExecutorService() {
        NamedThreadFactory threadFactory = new NamedThreadFactory("joyqueue-stat-save-scheduler");
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
}