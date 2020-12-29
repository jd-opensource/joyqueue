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
package io.openmessaging.spring.cloud.stream.binder.integration;

import io.openmessaging.extension.QueueMetaData;
import io.openmessaging.joyqueue.consumer.ExtensionConsumer;
import io.openmessaging.message.Message;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSBinderConfigurationProperties;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSConsumerProperties;
import io.openmessaging.spring.cloud.stream.binder.utils.MessageUtil;
import io.openmessaging.spring.support.AccessPointContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.context.Lifecycle;
import org.springframework.integration.endpoint.AbstractMessageSource;

import java.util.Objects;

/**
 * OMS Message Source
 */
public class OMSMessageSource extends AbstractMessageSource<Object> implements DisposableBean, Lifecycle {

    private static final Log log = LogFactory.getLog(OMSMessageSource.class);

    private boolean running;

    private ExtensionConsumer consumer;

    private final AccessPointContainer accessPointContainer;

    private final OMSBinderConfigurationProperties omsBinderConfigurationProperties;

    private final ExtendedConsumerProperties<OMSConsumerProperties> omsConsumerProperties;

    private final String topic;

    private final String group;

    public OMSMessageSource(AccessPointContainer accessPointContainer, OMSBinderConfigurationProperties omsBinderConfigurationProperties,
                            ExtendedConsumerProperties<OMSConsumerProperties> omsConsumerProperties,
                            String topic, String group) {
        this.accessPointContainer = accessPointContainer;
        this.omsBinderConfigurationProperties = omsBinderConfigurationProperties;
        this.omsConsumerProperties = omsConsumerProperties;
        this.topic = topic;
        this.group = group;
    }

    @Override
    public synchronized void start() {
        if (this.isRunning()) {
            throw new IllegalStateException("pull consumer already running. " + this.toString());
        }
        try {
            consumer = (ExtensionConsumer) accessPointContainer.getAccessPoint().createConsumer();
            consumer.bindQueue(topic);
            consumer.start();
        } catch (Exception e) {
            log.error("Start poll consumer error! " + e.getMessage(), e);
        }
        running = true;
    }

    @Override
    public synchronized void stop() {
        if (this.isRunning()) {
            this.running = false;
            consumer.stop();
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    protected synchronized Object doReceive() {
        try {
            //TODO 未完成
            QueueMetaData queueMetaData = consumer.getQueueMetaData(topic);
            for (QueueMetaData.Partition partition : queueMetaData.partitions()) {
                Message message = consumer.receive((short) partition.partitionId(), 1000 * 10);
                if (Objects.nonNull(message)) {
                    return MessageUtil.convert2SpringMessage(message);
                }
            }
        } catch (Exception e) {
            log.error("Consumer pull error: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getComponentType() {
        return "joyqueue:message-source";
    }
}
