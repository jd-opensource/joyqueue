/**
 * Copyright 2019 The JoyQueue Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.spring.cloud.stream.binder.consuming;

import io.openmessaging.consumer.BatchMessageListener;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.consumer.MessageListener;
import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.spring.cloud.stream.binder.OMSMessageChannelBinder;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSBinderConfigurationProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSConsumerProperties;
import io.openmessaging.spring.support.AccessPointContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * OMS Listener Binding Container
 */
public class OMSListenerBindingContainer implements InitializingBean, DisposableBean, SmartLifecycle {

    private MessageListener messageListener;

    private BatchMessageListener batchMessageListener;

    private Consumer consumer;

    private String consumerGroup;

    private String topic;

    private int consumeThreadMax = 64;

    private boolean running;

    private final ExtendedConsumerProperties<OMSConsumerProperties> omsConsumerProperties;

    private final OMSMessageChannelBinder omsMessageChannelBinder;

    private final OMSBinderConfigurationProperties omsBinderConfigurationProperties;

    private final AccessPointContainer accessPointContainer;

    public OMSListenerBindingContainer(ExtendedConsumerProperties<OMSConsumerProperties> omsConsumerProperties,
                                       OMSBinderConfigurationProperties omsBinderConfigurationProperties,
                                       OMSMessageChannelBinder omsMessageChannelBinder,
                                       AccessPointContainer accessPointContainer) {
        this.omsConsumerProperties = omsConsumerProperties;
        this.omsMessageChannelBinder = omsMessageChannelBinder;
        this.omsBinderConfigurationProperties = omsBinderConfigurationProperties;
        this.accessPointContainer = accessPointContainer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(consumerGroup, "Property 'consumerGroup' is required");
        Assert.notNull(topic, "Property 'topic' is required");
        if (null == messageListener && null == batchMessageListener) {
            throw new IllegalStateException("No available listener!");
        }
        Consumer consumer = accessPointContainer.getAccessPoint().createConsumer();
        consumer.start();
        for (ConsumerInterceptor interceptor : accessPointContainer.getConsumerInterceptors()) {
            consumer.addInterceptor(interceptor);
        }
        if (null != batchMessageListener && omsConsumerProperties.getExtension().isBatch()) {
            consumer.bindQueue(topic, batchMessageListener);
        } else {
            consumer.bindQueue(topic, messageListener);
        }
        this.consumer = consumer;
    }

    @Override
    public void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("Container already running. " + this.toString());
        }
        try {
            consumer.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start consumer", e);
        }
        this.setRunning(true);
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            if (Objects.nonNull(consumer)) {
                consumer.stop();
            }
            setRunning(false);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void destroy() throws Exception {
        this.setRunning(false);
        if (Objects.nonNull(consumer)) {
            consumer.stop();
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setConsumeThreadMax(int consumeThreadMax) {
        this.consumeThreadMax = consumeThreadMax;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setBatchMessageListener(BatchMessageListener batchMessageListener) {
        this.batchMessageListener = batchMessageListener;
    }
}
