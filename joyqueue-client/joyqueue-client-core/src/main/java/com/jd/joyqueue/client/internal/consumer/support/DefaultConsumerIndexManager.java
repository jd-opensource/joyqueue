/**
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
package com.jd.joyqueue.client.internal.consumer.support;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.jd.joyqueue.client.internal.cluster.ClusterManager;
import com.jd.joyqueue.client.internal.consumer.ConsumerIndexManager;
import com.jd.joyqueue.client.internal.consumer.domain.ConsumeReply;
import com.jd.joyqueue.client.internal.consumer.domain.FetchIndexData;
import com.jd.joyqueue.client.internal.consumer.transport.ConsumerClient;
import com.jd.joyqueue.client.internal.consumer.transport.ConsumerClientManager;
import com.jd.joyqueue.client.internal.exception.ClientException;
import com.jd.joyqueue.client.internal.metadata.domain.PartitionMetadata;
import com.jd.joyqueue.client.internal.metadata.domain.TopicMetadata;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.network.command.CommitAckResponse;
import com.jd.joyqueue.network.command.CommitAckData;
import com.jd.joyqueue.network.command.FetchIndexResponse;
import com.jd.joyqueue.network.command.FetchIndexAckData;
import com.jd.joyqueue.network.domain.BrokerNode;
import com.jd.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * DefaultConsumerIndexManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/12
 */
// TODO 优化代码
public class DefaultConsumerIndexManager extends Service implements ConsumerIndexManager {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultConsumerIndexManager.class);

    private ClusterManager clusterManager;
    private ConsumerClientManager consumerClientManager;

    public DefaultConsumerIndexManager(ClusterManager clusterManager, ConsumerClientManager consumerClientManager) {
        this.clusterManager = clusterManager;
        this.consumerClientManager = consumerClientManager;
    }

    @Override
    public JournalqCode resetIndex(String topic, String app, short partition, long timeout) {
        return JournalqCode.SUCCESS;
    }

    @Override
    public FetchIndexData fetchIndex(String topic, String app, short partition, long timeout) {
        Map<String, List<Short>> topicMap = Maps.newHashMap();
        topicMap.put(topic, Lists.newArrayList(partition));
        Table<String, Short, FetchIndexData> batchFetchIndexResult = batchFetchIndex(topicMap, app, timeout);
        return batchFetchIndexResult.get(topic, partition);
    }

    @Override
    public JournalqCode commitReply(String topic, List<ConsumeReply> replyList, String app, long timeout) {
        Map<String, List<ConsumeReply>> topicMap = Maps.newHashMap();
        topicMap.put(topic, replyList);
        Map<String, JournalqCode> batchCommitReplyResult = batchCommitReply(topicMap, app, timeout);
        return batchCommitReplyResult.get(topic);
    }

    @Override
    public Table<String, Short, FetchIndexData> batchFetchIndex(Map<String, List<Short>> topicMap, String app, long timeout) {
        Table<String, Short, FetchIndexData> result = HashBasedTable.create();
        if (MapUtils.isEmpty(topicMap)) {
            return result;
        }

        Map<BrokerNode, Map<String, List<Short>>> brokerFetchMap = buildFetchIndexParams(topicMap, app);
        for (Map.Entry<BrokerNode, Map<String, List<Short>>> entry : brokerFetchMap.entrySet()) {
            try {
                ConsumerClient client = consumerClientManager.getOrCreateClient(entry.getKey());
                FetchIndexResponse fetchIndexResponse = client.fetchIndex(entry.getValue(), app, timeout);

                for (Map.Entry<String, Map<Short, FetchIndexAckData>> topicEntry : fetchIndexResponse.getData().rowMap().entrySet()) {
                    for (Map.Entry<Short, FetchIndexAckData> partitionEntry : topicEntry.getValue().entrySet()) {
                        result.put(topicEntry.getKey(), partitionEntry.getKey(), new FetchIndexData(partitionEntry.getValue().getIndex(), partitionEntry.getValue().getCode()));
                    }
                }
            } catch (ClientException e) {
                logger.error("fetchIndex exception, fetchMap: {}, app: {}", entry.getValue(), app, e);
                for (Map.Entry<String, List<Short>> topicEntry : entry.getValue().entrySet()) {
                    for (Short partition : topicEntry.getValue()) {
                        result.put(topicEntry.getKey(), partition, new FetchIndexData(JournalqCode.valueOf(e.getCode())));
                    }
                }
            }
        }

        for (Map.Entry<String, List<Short>> entry : topicMap.entrySet()) {
            for (Short partition : entry.getValue()) {
                if (result.contains(entry.getKey(), partition)) {
                    continue;
                }
                result.put(entry.getKey(), partition, new FetchIndexData(JournalqCode.CN_UNKNOWN_ERROR));
            }
        }

        return result;
    }

    @Override
    public Map<String, JournalqCode> batchCommitReply(Map<String, List<ConsumeReply>> replyMap, String app, long timeout) {
        Map<String, JournalqCode> result = Maps.newHashMap();
        Map<BrokerNode, Table<String, Short, List<CommitAckData>>> brokerCommitMap = buildCommitAckParams(replyMap, app);
        for (Map.Entry<BrokerNode, Table<String, Short, List<CommitAckData>>> entry : brokerCommitMap.entrySet()) {
            try {
                ConsumerClient client = consumerClientManager.getOrCreateClient(entry.getKey());
                CommitAckResponse commitAckResponse = client.commitAck(entry.getValue(), app, timeout);

                for (Map.Entry<String, Map<Short, JournalqCode>> resultEntry : commitAckResponse.getResult().rowMap().entrySet()) {
                    for (Map.Entry<Short, JournalqCode> ackEntry : resultEntry.getValue().entrySet()) {
                        result.put(resultEntry.getKey(), ackEntry.getValue());
                    }
                }
            } catch (ClientException e) {
                logger.error("commit ack exception, commitMap: {}, app: {}", entry.getValue(), app, e);
                for (Map.Entry<String, Map<Short, List<CommitAckData>>> topicEntry : entry.getValue().rowMap().entrySet()) {
                    result.put(topicEntry.getKey(), JournalqCode.valueOf(e.getCode()));
                }
            }
        }
        for (Map.Entry<String, List<ConsumeReply>> entry : replyMap.entrySet()) {
            if (result.containsKey(entry.getKey())) {
                continue;
            }
            result.put(entry.getKey(), JournalqCode.CN_UNKNOWN_ERROR);
        }
        return result;
    }

    protected Map<BrokerNode, Map<String, List<Short>>> buildFetchIndexParams(Map<String, List<Short>> topicMap, String app) {
        Map<BrokerNode, Map<String, List<Short>>> result = Maps.newHashMap();

        for (Map.Entry<String, List<Short>> entry : topicMap.entrySet()) {
            String topic = entry.getKey();
            TopicMetadata topicMetadata = clusterManager.fetchTopicMetadata(topic, app);
            if (topicMetadata == null) {
                logger.warn("topic {} metadata is null", topic);
                continue;
            }

            for (Short partition : entry.getValue()) {
                PartitionMetadata partitionMetadata = topicMetadata.getPartition(partition);
                if (partitionMetadata == null) {
                    partitionMetadata = topicMetadata.getPartitions().get(0);
                }

                BrokerNode leader = partitionMetadata.getLeader();
                if (leader == null) {
                    logger.warn("topic {}, partition {}, leader is null", topic, partition);
                    continue;
                }

                Map<String, List<Short>> topicPartitionMap = result.get(leader);
                if (topicPartitionMap == null) {
                    topicPartitionMap = Maps.newHashMap();
                    result.put(leader, topicPartitionMap);
                }

                List<Short> partitions = topicPartitionMap.get(topic);
                if (partitions == null) {
                    partitions = Lists.newLinkedList();
                    topicPartitionMap.put(topic, partitions);
                }

                partitions.add(partition);
            }
        }

        return result;
    }

    protected Map<BrokerNode, Table<String, Short, List<CommitAckData>>> buildCommitAckParams(Map<String, List<ConsumeReply>> ackMap, String app) {
        Map<BrokerNode, Table<String, Short, List<CommitAckData>>> result = Maps.newHashMap();

        for (Map.Entry<String, List<ConsumeReply>> entry : ackMap.entrySet()) {
            String topic = entry.getKey();
            TopicMetadata topicMetadata = clusterManager.fetchTopicMetadata(topic, app);

            if (topicMetadata == null) {
                logger.warn("topic {} metadata is null", topic);
                continue;
            }

            for (ConsumeReply consumeReply : entry.getValue()) {
                PartitionMetadata partitionMetadata = topicMetadata.getPartition(consumeReply.getPartition());
                if (partitionMetadata == null) {
                    partitionMetadata = topicMetadata.getPartitions().get(0);
                }

                BrokerNode leader = partitionMetadata.getLeader();
                if (leader == null) {
                    logger.warn("topic {}, partition {}, leader is null", topic, consumeReply.getPartition());
                    continue;
                }

                Table<String, Short, List<CommitAckData>> topicConsumeAckTable = result.get(leader);
                if (topicConsumeAckTable == null) {
                    topicConsumeAckTable = HashBasedTable.create();
                    result.put(leader, topicConsumeAckTable);
                }

                List<CommitAckData> commitAckList = topicConsumeAckTable.get(topic, consumeReply.getPartition());
                if (commitAckList == null) {
                    commitAckList = Lists.newLinkedList();
                    topicConsumeAckTable.put(topic, consumeReply.getPartition(), commitAckList);
                }

                commitAckList.add(new CommitAckData(consumeReply.getPartition(), consumeReply.getIndex(), consumeReply.getRetryType()));
            }
        }

        return result;
    }
}