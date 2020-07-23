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
package org.joyqueue.broker.manage.service.support;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.MessageConvertSupport;
import org.joyqueue.broker.manage.exception.ManageException;
import org.joyqueue.broker.manage.service.MessageManageService;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.SourceType;
import org.joyqueue.monitor.BrokerMessageInfo;
import org.joyqueue.network.session.Consumer;
import org.joyqueue.store.StoreManagementService;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * MessageManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/18
 */
public class DefaultMessageManageService implements MessageManageService {

    private Consume consume;
    private StoreManagementService storeManagementService;
    private MessageConvertSupport messageConvertSupport;

    public DefaultMessageManageService(Consume consume, StoreManagementService storeManagementService, MessageConvertSupport messageConvertSupport) {
        this.consume = consume;
        this.storeManagementService = storeManagementService;
        this.messageConvertSupport = messageConvertSupport;
    }

    @Override
    public List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count) {
        try {
            List<BrokerMessage> brokerMessages = Lists.newArrayListWithCapacity(count);
            List<BrokerMessageInfo> result = Lists.newArrayListWithCapacity(count);
            byte[][] bytes = storeManagementService.readMessages(topic, partition, index, count);
            if (ArrayUtils.isNotEmpty(bytes)) {
                for (byte[] message : bytes) {
                    brokerMessages.add(Serializer.readBrokerMessage(ByteBuffer.wrap(message)));
                }
            }

            brokerMessages = messageConvertSupport.convert(brokerMessages, SourceType.INTERNAL.getValue());
            for (BrokerMessage brokerMessage : brokerMessages) {
                result.add(new BrokerMessageInfo(brokerMessage));
            }
            return result;
        } catch (Exception e) {
            throw new ManageException(e);
        }
    }

    @Override
    public List<BrokerMessageInfo> getPendingMessage(String topic, String app, int count) {
        try {
            Consumer consumer = new Consumer();
            consumer.setTopic(topic);
            consumer.setApp(app);

            List<BrokerMessageInfo> result = Lists.newArrayListWithCapacity(count);
            StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
            for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                    int realCount = count - result.size();
                    if (realCount <= 0) {
                        break;
                    }
                    long ackIndex = consume.getAckIndex(consumer, partitionMetric.getPartition());
                    if (ackIndex < 0) {
                        ackIndex = 0;
                    }
                    if (ackIndex >= partitionMetric.getRightIndex()) {
                        continue;
                    }
                    List<BrokerMessage> brokerMessages = Lists.newLinkedList();
                    byte[][] bytes = storeManagementService.readMessages(topic, partitionMetric.getPartition(), ackIndex, realCount);
                    if (ArrayUtils.isNotEmpty(bytes)) {
                        for (byte[] message : bytes) {
                            BrokerMessage brokerMessage = Serializer.readBrokerMessage(ByteBuffer.wrap(message));
                            brokerMessages.add(brokerMessage);
                        }
                    }

                    brokerMessages = messageConvertSupport.convert(brokerMessages, SourceType.INTERNAL.getValue());
                    for (BrokerMessage brokerMessage : brokerMessages) {
                        result.add(new BrokerMessageInfo(brokerMessage, (ackIndex > brokerMessage.getMsgIndexNo())));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            throw new ManageException(e);
        }
    }

    @Override
    public List<BrokerMessageInfo> getLastMessage(String topic, String app, int count) {
        try {
            Consumer consumer = new Consumer();
            consumer.setTopic(topic);
            consumer.setApp(app);

            List<BrokerMessageInfo> result = Lists.newArrayListWithCapacity(count);
            StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
            for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                    if (partitionMetric.getRightIndex() == 0) {
                        continue;
                    }
                    int realCount = count - result.size();
                    if (realCount <= 0) {
                        break;
                    }
                    long ackIndex = consume.getAckIndex(consumer, partitionMetric.getPartition());
                    long realIndex = ackIndex;
                    if (realIndex < 0) {
                        realIndex = 0;
                    }
                    if (realIndex >= partitionMetric.getRightIndex()) {
                        realIndex = partitionMetric.getRightIndex() - realCount;
                        realIndex = Math.max(realIndex, 0);
                    }
                    List<BrokerMessage> brokerMessages = Lists.newLinkedList();
                    byte[][] bytes = storeManagementService.readMessages(topic, partitionMetric.getPartition(), realIndex, realCount);
                    if (ArrayUtils.isNotEmpty(bytes)) {
                        for (byte[] message : bytes) {
                            BrokerMessage brokerMessage = Serializer.readBrokerMessage(ByteBuffer.wrap(message));
                            brokerMessages.add(brokerMessage);
                        }
                    }

                    brokerMessages = messageConvertSupport.convert(brokerMessages, SourceType.INTERNAL.getValue());
                    for (BrokerMessage brokerMessage : brokerMessages) {
                        result.add(new BrokerMessageInfo(brokerMessage, (ackIndex > brokerMessage.getMsgIndexNo())));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new ManageException(e);
        }
    }

    @Override
    public List<BrokerMessageInfo> viewMessage(String topic, String app, int count) {
        List<BrokerMessageInfo> pendingMessage = getPendingMessage(topic, app, count);
        if (CollectionUtils.isNotEmpty(pendingMessage)) {
            return pendingMessage;
        }
        return getLastMessage(topic, app, count);
    }
}