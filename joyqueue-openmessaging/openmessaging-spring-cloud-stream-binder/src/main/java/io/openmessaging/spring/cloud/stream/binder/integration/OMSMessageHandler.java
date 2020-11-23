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
import io.openmessaging.producer.Producer;
import io.openmessaging.spring.cloud.stream.binder.properties.OMSProducerProperties;
import io.openmessaging.spring.cloud.stream.binder.utils.MessageUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binding.MessageConverterConfigurer;
import org.springframework.context.Lifecycle;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;

/**
 * OMS Message Handler
 */
public class OMSMessageHandler extends AbstractMessageHandler implements Lifecycle {

    private static final Log log = LogFactory.getLog(OMSMessageHandler.class);

    private final Producer producer;

    private final boolean transactional;

    private final String destination;

    private final String groupName;

    private volatile boolean running = false;

    private ExtendedProducerProperties<OMSProducerProperties> producerProperties;

    private MessageConverterConfigurer.PartitioningInterceptor partitioningInterceptor;

    public OMSMessageHandler(Producer producer, String destination, String groupName, Boolean transactional,
                             ExtendedProducerProperties<OMSProducerProperties> producerProperties,
                             MessageConverterConfigurer.PartitioningInterceptor partitioningInterceptor) {
        Assert.notNull(producer, "Producer is required");
        Assert.notNull(destination, "Destination is required");
        this.producer = producer;
        this.destination = destination;
        this.groupName = groupName;
        this.transactional = transactional;
        this.producerProperties = producerProperties;
        this.partitioningInterceptor = partitioningInterceptor;
    }

    @Override
    public void start() {
        this.producer.start();
        running = true;
    }

    @Override
    public void stop() {
        if (!transactional) {
            producer.stop();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        try {
            QueueMetaData queueMetaData = producer.getQueueMetaData(destination);
            // for (QueueMetaData.Partition partition : queueMetaData.partitions()) {
            //     if (log.isInfoEnabled()) {
            //         log.info(String.format("partition: %s, partitionHost: %s", partition.partitionId(), partition.partitonHost()));
            //     }
            // }
            QueueMetaData.Partition partition = queueMetaData.partitions().get((int) (System.currentTimeMillis() % queueMetaData.partitions().size()));
            io.openmessaging.message.Message omsMessage = MessageUtil.convert2OMSMessage(producer, destination, message);
            omsMessage.extensionHeader().get().setPartition(partition.partitionId());
            producer.send(omsMessage);
        } catch (Exception e) {
            throw new MessagingException(message, e);
        }
    }
}
