package com.jd.journalq.broker.mqtt.config;

import com.jd.journalq.broker.mqtt.handler.*;
import com.jd.journalq.broker.mqtt.util.ExecutorServiceFactory;
import com.jd.journalq.broker.mqtt.handler.*;
import com.jd.journalq.toolkit.service.Service;

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
