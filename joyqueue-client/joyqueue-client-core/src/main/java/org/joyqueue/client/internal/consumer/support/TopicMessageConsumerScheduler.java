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
package org.joyqueue.client.internal.consumer.support;

import org.joyqueue.client.internal.consumer.MessagePoller;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TopicMessageConsumerScheduler
 *
 * author: gaohaoxiang
 * date: 2018/12/25
 */
public class TopicMessageConsumerScheduler extends Service implements Runnable {

    protected static final Logger logger = LoggerFactory.getLogger(TopicMessageConsumerScheduler.class);

    private String topic;
    private ConsumerConfig config;
    private MessagePoller messagePoller;
    private TopicMessageConsumerDispatcher messageConsumerDispatcher;
    private ExecutorService scheduleThreadPool;
    private volatile boolean suspend = false;
    private volatile boolean stopped = false;

    public TopicMessageConsumerScheduler(String topic, ConsumerConfig config, MessagePoller messagePoller, TopicMessageConsumerDispatcher messageConsumerDispatcher) {
        this.topic = topic;
        this.config = config;
        this.messagePoller = messagePoller;
        this.messageConsumerDispatcher = messageConsumerDispatcher;
    }

    @Override
    protected void validate() throws Exception {
        scheduleThreadPool = Executors.newFixedThreadPool(config.getThread(), new NamedThreadFactory(String.format("joyqueue-consumer-scheduler-%s", topic), true));
    }

    @Override
    protected void doStart() throws Exception {
        for (int i = 0; i < config.getThread(); i++) {
            scheduleThreadPool.execute(this);
        }

//        logger.info("{} consumer is started", topic);
    }

    @Override
    protected void doStop() {
        stopped = true;
        if (scheduleThreadPool != null) {
            scheduleThreadPool.shutdown();
        }

//        logger.info("{} consumer is stopped", topic);
    }

    public void suspend() {
        suspend = true;
    }

    public boolean isSuspend() {
        return suspend;
    }

    public void resume() {
        suspend = false;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                if (suspend) {
                    Thread.currentThread().sleep(config.getIdleInterval());
                    continue;
                }
                doSchedule();
            } catch (Exception e) {
                if (stopped) {
                    continue;
                }
                logger.error("dispatch consumer exception, topic: {}", topic, e);
                try {
                    Thread.currentThread().sleep(config.getIdleInterval());
                } catch (InterruptedException e1) {
                    logger.debug("dispatch consumer exception, topic: {}", topic, e1);
                }
            }
        }
    }

    protected void doSchedule() throws Exception {
        boolean result = messageConsumerDispatcher.dispatch();
        if (result) {
            if (config.getInterval() > 0) {
                Thread.currentThread().sleep(config.getInterval());
            }
        } else {
            Thread.currentThread().sleep(config.getIdleInterval());
        }
    }
}