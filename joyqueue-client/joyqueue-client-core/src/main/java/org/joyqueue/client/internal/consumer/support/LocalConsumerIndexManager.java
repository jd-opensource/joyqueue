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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.commons.collections.MapUtils;
import org.joyqueue.client.internal.consumer.ConsumerIndexManager;
import org.joyqueue.client.internal.consumer.config.ConsumerConfig;
import org.joyqueue.client.internal.consumer.domain.ConsumeReply;
import org.joyqueue.client.internal.consumer.domain.FetchIndexData;
import org.joyqueue.client.internal.consumer.domain.LocalIndexData;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * LocalConsumerIndexManager
 *
 * author: gaohaoxiang
 * date: 2018/12/14
 */
// TODO 代码优化
public class LocalConsumerIndexManager extends Service implements ConsumerIndexManager {

    protected static final Logger logger = LoggerFactory.getLogger(LocalConsumerIndexManager.class);

    private ConsumerConfig config;
    private ConsumerIndexManager delegate;
    private ConsumerLocalIndexStore consumerIndexStore;

    public LocalConsumerIndexManager(ConsumerConfig config, ConsumerIndexManager delegate) {
        this.config = config;
        this.delegate = delegate;
    }

    @Override
    protected void validate() throws Exception {
        consumerIndexStore = new ConsumerLocalIndexStore(config.getBroadcastLocalPath() + File.separator + config.getBroadcastGroup(), config.getBroadcastPersistInterval());
    }

    @Override
    protected void doStart() throws Exception {
        consumerIndexStore.start();
    }

    @Override
    protected void doStop() {
        if (consumerIndexStore != null) {
            consumerIndexStore.stop();
        }
    }

    @Override
    public JoyQueueCode resetIndex(String topic, String app, short partition, long timeout) {
        FetchIndexData fetchIndexData = delegate.fetchIndex(topic, app, partition, timeout);
        if (!fetchIndexData.getCode().equals(JoyQueueCode.SUCCESS)) {
            logger.error("resetIndex error, topic: {}, partition: {}, error: {}", topic, partition, fetchIndexData.getCode());
            return fetchIndexData.getCode();
        }
        long index = 0;
        if (config.getBroadcastIndexAutoReset() == ConsumerConfig.BROADCAST_AUTO_RESET_CURRENT_INDEX) {
            index = fetchIndexData.getIndex();
        } else if (config.getBroadcastIndexAutoReset() == ConsumerConfig.BROADCAST_AUTO_RESET_LEFT_INDEX) {
            index = fetchIndexData.getLeftIndex();
        } else if (config.getBroadcastIndexAutoReset() == ConsumerConfig.BROADCAST_AUTO_RESET_RIGHT_INDEX) {
            index = fetchIndexData.getRightIndex();
        }
        consumerIndexStore.saveIndex(topic, app, partition, index);
        return JoyQueueCode.SUCCESS;
    }

    @Override
    public FetchIndexData fetchIndex(String topic, String app, short partition, long timeout) {
        LocalIndexData localIndexData = consumerIndexStore.fetchIndex(topic, app, partition);
        if (localIndexData != null && !isExpired(localIndexData)) {
            return new FetchIndexData(localIndexData.getIndex(), 0, 0, JoyQueueCode.SUCCESS);
        } else {
            FetchIndexData fetchIndexData = delegate.fetchIndex(topic, app, partition, timeout);
            if (!fetchIndexData.getCode().equals(JoyQueueCode.SUCCESS)) {
                logger.error("batchFetch index error, topic: {}, partition: {}, error: {}", topic, partition, fetchIndexData.getCode());
                return new FetchIndexData(-1, 0, 0, JoyQueueCode.SUCCESS);
            }
            long index = 0;
            if (config.getBroadcastIndexAutoReset() == ConsumerConfig.BROADCAST_AUTO_RESET_CURRENT_INDEX) {
                index = fetchIndexData.getIndex();
            } else if (config.getBroadcastIndexAutoReset() == ConsumerConfig.BROADCAST_AUTO_RESET_LEFT_INDEX) {
                index = fetchIndexData.getLeftIndex();
            } else if (config.getBroadcastIndexAutoReset() == ConsumerConfig.BROADCAST_AUTO_RESET_RIGHT_INDEX) {
                index = fetchIndexData.getRightIndex();
            }
            consumerIndexStore.saveIndex(topic, app, partition, index);
            return new FetchIndexData(index, 0, 0, JoyQueueCode.SUCCESS);
        }
    }

    @Override
    public JoyQueueCode commitReply(String topic, List<ConsumeReply> replyList, String app, long timeout) {
        for (ConsumeReply reply : replyList) {
            consumerIndexStore.saveIndex(topic, app, reply.getPartition(), reply.getIndex() + 1);
        }
        return JoyQueueCode.SUCCESS;
    }

    @Override
    public JoyQueueCode commitIndex(String topic, String app, short partition, long index, long timeout) {
        if (index == ConsumerIndexManager.MAX_INDEX) {
            FetchIndexData fetchIndexData = delegate.fetchIndex(topic, app, partition, config.getTimeout());
            index = fetchIndexData.getRightIndex();
        } else if (index == ConsumerIndexManager.MIN_INDEX) {
            FetchIndexData fetchIndexData = delegate.fetchIndex(topic, app, partition, config.getTimeout());
            index = fetchIndexData.getLeftIndex();
        }
        consumerIndexStore.saveIndex(topic, app, partition, index);
        return JoyQueueCode.SUCCESS;
    }

    @Override
    public Table<String, Short, FetchIndexData> batchFetchIndex(Map<String, List<Short>> topicMap, String app, long timeout) {
        Table<String, Short, FetchIndexData> result = HashBasedTable.create();
        Map<String, List<Short>> updateTopicMap = null;

        for (Map.Entry<String, List<Short>> topicEntry : topicMap.entrySet()) {
            String topic = topicEntry.getKey();
            for (Short partition : topicEntry.getValue()) {
                FetchIndexData fetchIndexData = delegate.fetchIndex(topic, app, partition, timeout);
                LocalIndexData localIndexData = consumerIndexStore.fetchIndex(topic, app, partition);
                if (localIndexData == null || isExpired(localIndexData)) {
                    if (updateTopicMap == null) {
                        updateTopicMap = Maps.newHashMap();
                    }
                    List<Short> partitions = updateTopicMap.get(topic);
                    if (partitions == null) {
                        partitions = Lists.newLinkedList();
                        updateTopicMap.put(topic, partitions);
                    }
                    partitions.add(partition);
                } else {
                    result.put(topic, partition, new FetchIndexData(localIndexData.getIndex(), fetchIndexData.getLeftIndex(), fetchIndexData.getRightIndex(), JoyQueueCode.SUCCESS));
                }
            }
        }

        if (MapUtils.isNotEmpty(updateTopicMap)) {
            Table<String, Short, FetchIndexData> fetchIndexTable = delegate.batchFetchIndex(updateTopicMap, app, timeout);
            for (Map.Entry<String, List<Short>> topicEntry : updateTopicMap.entrySet()) {
                String topic = topicEntry.getKey();
                for (Short partition : topicEntry.getValue()) {
                    FetchIndexData fetchIndexData = fetchIndexTable.get(topic, partition);
                    if (!fetchIndexData.getCode().equals(JoyQueueCode.SUCCESS)) {
                        logger.error("batchFetch index error, topic: {}, partition: {}, error: {}", topic, partition, fetchIndexData.getCode());
                    } else {
                        consumerIndexStore.saveIndex(topic, app, partition, fetchIndexData.getIndex());
                    }
                    result.put(topic, partition, fetchIndexData);
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, JoyQueueCode> batchCommitReply(Map<String, List<ConsumeReply>> replyMap, String app, long timeout) {
        Map<String, JoyQueueCode> result = Maps.newHashMap();
        for (Map.Entry<String, List<ConsumeReply>> entry : replyMap.entrySet()) {
            String topic = entry.getKey();
            for (ConsumeReply consumeReply : entry.getValue()) {
                consumerIndexStore.saveIndex(topic, app, consumeReply.getPartition(), consumeReply.getIndex());
            }
        }
        return result;
    }

    @Override
    public Map<Short, JoyQueueCode> batchCommitIndex(String topic, String app, Map<Short, Long> indexes, long timeout) {
        Map<Short, JoyQueueCode> result = Maps.newHashMap();
        Map<Short, FetchIndexData> fetchIndexDataMap = null;

        for (Map.Entry<Short, Long> entry : indexes.entrySet()) {
            if (entry.getValue().equals(ConsumerIndexManager.MAX_INDEX) || entry.getValue().equals(ConsumerIndexManager.MIN_INDEX)) {
                Map<String, List<Short>> param = Maps.newHashMap();
                param.put(topic, Lists.newArrayList(indexes.keySet()));

                fetchIndexDataMap = delegate.batchFetchIndex(param, app, timeout).row(topic);
                break;
            }
        }

        for (Map.Entry<Short, Long> entry : indexes.entrySet()) {
            short partition = entry.getKey();
            long index = entry.getValue();

            if (index == ConsumerIndexManager.MAX_INDEX) {
                if (fetchIndexDataMap == null || fetchIndexDataMap.get(partition) == null) {
                    result.put(entry.getKey(), JoyQueueCode.CN_UNKNOWN_ERROR);
                    continue;
                }
                FetchIndexData fetchIndexData = fetchIndexDataMap.get(partition);
                index = fetchIndexData.getRightIndex();
            } else if (index == ConsumerIndexManager.MIN_INDEX) {
                if (fetchIndexDataMap == null || fetchIndexDataMap.get(partition) == null) {
                    result.put(entry.getKey(), JoyQueueCode.CN_UNKNOWN_ERROR);
                    continue;
                }
                FetchIndexData fetchIndexData = fetchIndexDataMap.get(partition);
                index = fetchIndexData.getLeftIndex();
            }

            consumerIndexStore.saveIndex(topic, app, partition, index);
            result.put(entry.getKey(), JoyQueueCode.SUCCESS);
        }
        return result;
    }

    protected boolean isExpired(LocalIndexData localIndexData) {
        if (config.getBroadcastIndexExpireTime() == ConsumerConfig.NONE_BROADCAST_INDEX_EXPIRE_TIME) {
            return false;
        }
        return localIndexData.isExpired(config.getBroadcastIndexExpireTime());
    }
}