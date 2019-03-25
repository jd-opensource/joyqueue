package com.jd.journalq.broker.manage.service.support;

import com.google.common.collect.Lists;
import com.jd.journalq.broker.buffer.Serializer;
import com.jd.journalq.broker.consumer.Consume;
import com.jd.journalq.broker.manage.exception.ManageException;
import com.jd.journalq.broker.manage.service.MessageManageService;
import com.jd.journalq.monitor.BrokerMessageInfo;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.store.StoreManagementService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * MessageManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/18
 */
public class DefaultMessageManageService implements MessageManageService {

    private Consume consume;
    private StoreManagementService storeManagementService;

    public DefaultMessageManageService(Consume consume, StoreManagementService storeManagementService) {
        this.consume = consume;
        this.storeManagementService = storeManagementService;
    }

    @Override
    public List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count) {
        try {
            List<BrokerMessageInfo> result = Lists.newArrayListWithCapacity(count);
            byte[][] bytes = storeManagementService.readMessages(topic, partition, index, count);
            if (ArrayUtils.isNotEmpty(bytes)) {
                for (byte[] message : bytes) {
                    result.add(new BrokerMessageInfo(Serializer.readBrokerMessage(ByteBuffer.wrap(message))));
                }
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
                    byte[][] bytes = storeManagementService.readMessages(topic, partitionMetric.getPartition(), ackIndex, realCount);
                    if (ArrayUtils.isNotEmpty(bytes)) {
                        for (byte[] message : bytes) {
                            result.add(new BrokerMessageInfo(Serializer.readBrokerMessage(ByteBuffer.wrap(message))));
                        }
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
            List<BrokerMessageInfo> result = Lists.newArrayListWithCapacity(count);
            StoreManagementService.TopicMetric topicMetric = storeManagementService.topicMetric(topic);
            for (StoreManagementService.PartitionGroupMetric partitionGroupMetric : topicMetric.getPartitionGroupMetrics()) {
                for (StoreManagementService.PartitionMetric partitionMetric : partitionGroupMetric.getPartitionMetrics()) {
                    int realCount = count - result.size();
                    if (realCount <= 0) {
                        break;
                    }
                    long realIndex = 0L;
                    if (count > partitionMetric.getRightIndex()) {
                        realIndex = 0;
                    } else {
                        realIndex = partitionMetric.getRightIndex() - realCount;
                    }
                    byte[][] bytes = storeManagementService.readMessages(topic, partitionMetric.getPartition(), realIndex, realCount);
                    if (ArrayUtils.isNotEmpty(bytes)) {
                        for (byte[] message : bytes) {
                            result.add(new BrokerMessageInfo(Serializer.readBrokerMessage(ByteBuffer.wrap(message))));
                        }
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