/**
 * Copyright 2018 The JoyQueue Authors.
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
package io.chubao.joyqueue.broker.mqtt.config;

import io.chubao.joyqueue.broker.mqtt.handler.ConnectHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PingReqHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PingRespHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PublishAckHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PublishCompHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PublishHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PublishRecHandler;
import io.chubao.joyqueue.broker.mqtt.handler.PublishRelHandler;
import io.chubao.joyqueue.broker.mqtt.handler.SubscribeHandler;
import io.chubao.joyqueue.broker.mqtt.handler.UnSubscribeHandler;
import io.chubao.joyqueue.broker.mqtt.util.ExecutorServiceFactory;
import io.chubao.joyqueue.toolkit.service.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author majun8
 */
public class MqttContext extends Service {

    private MqttConfig mqttConfig;
    private Map<Class, ExecutorService> executorServiceMap = new HashMap<>();

    public MqttContext(MqttConfig mqttConfig) {
        this.mqttConfig = mqttConfig;
    }

    private void loadContext() {
        ExecutorService connectExecutor = ExecutorServiceFactory.createExecutorService(
                mqttConfig.getConnectionThreadPoolSize(),
                mqttConfig.getConnectionThreadPoolQueueSize(),
                mqttConfig.getConnectionThreadPoolName()
        );
        executorServiceMap.put(
                ConnectHandler.class,
                connectExecutor
        );
        ExecutorService pingExecutor = ExecutorServiceFactory.createExecutorService(
                mqttConfig.getPingThreadPoolSize(),
                mqttConfig.getPingThreadPoolQueueSize(),
                mqttConfig.getPingThreadPoolName()
        );
        executorServiceMap.put(
                PingReqHandler.class,
                pingExecutor
        );
        executorServiceMap.put(
                PingRespHandler.class,
                pingExecutor
        );
        ExecutorService publishExecutor = ExecutorServiceFactory.createExecutorService(
                mqttConfig.getPublishThreadPoolSize(),
                mqttConfig.getPublishThreadPoolQueueSize(),
                mqttConfig.getPublishThreadPoolName()
        );
        executorServiceMap.put(
                PublishHandler.class,
                publishExecutor
        );
        executorServiceMap.put(
                PublishAckHandler.class,
                publishExecutor
        );
        executorServiceMap.put(
                PublishRecHandler.class,
                publishExecutor
        );
        executorServiceMap.put(
                PublishRelHandler.class,
                publishExecutor
        );
        executorServiceMap.put(
                PublishCompHandler.class,
                publishExecutor
        );
        ExecutorService subscriptionExecutor = ExecutorServiceFactory.createExecutorService(
                mqttConfig.getSubscriptionThreadPoolSize(),
                mqttConfig.getSubscriptionThreadPoolQueueSize(),
                mqttConfig.getSubscriptionThreadPoolName()
        );
        executorServiceMap.put(
                SubscribeHandler.class,
                subscriptionExecutor
        );
        executorServiceMap.put(
                UnSubscribeHandler.class,
                subscriptionExecutor
        );
    }

    public MqttConfig getMqttConfig() {
        return mqttConfig;
    }

    public Map<Class, ExecutorService> getExecutorServiceMap() {
        return executorServiceMap;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        loadContext();
    }

    @Override
    protected void doStop() {
        super.doStop();
        executorServiceMap.forEach(
                (cls, executorService) -> {
                    if (!executorService.isTerminated()) {
                        executorService.shutdown();
                    }
                }
        );
    }
}
