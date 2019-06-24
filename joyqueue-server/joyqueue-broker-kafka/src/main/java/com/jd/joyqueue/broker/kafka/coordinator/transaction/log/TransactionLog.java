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
package com.jd.joyqueue.broker.kafka.coordinator.transaction.log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.joyqueue.broker.cluster.ClusterManager;
import com.jd.joyqueue.broker.consumer.Consume;
import com.jd.joyqueue.broker.kafka.config.KafkaConfig;
import com.jd.joyqueue.broker.kafka.coordinator.Coordinator;
import com.jd.joyqueue.broker.kafka.coordinator.transaction.domain.TransactionDomain;
import com.jd.joyqueue.broker.kafka.coordinator.transaction.helper.TransactionSerializer;
import com.jd.joyqueue.broker.producer.Produce;
import com.jd.joyqueue.domain.PartitionGroup;
import com.jd.joyqueue.domain.TopicConfig;
import com.jd.joyqueue.exception.JournalqCode;
import com.jd.joyqueue.exception.JournalqException;
import com.jd.joyqueue.network.session.Consumer;
import com.jd.joyqueue.network.session.Producer;
import com.jd.joyqueue.toolkit.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * TransactionLog
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/4/15
 */
public class TransactionLog extends Service {

    protected static final Logger logger = LoggerFactory.getLogger(TransactionLog.class);

    private KafkaConfig config;
    private Produce produce;
    private Consume consume;
    private Coordinator coordinator;
    private ClusterManager clusterManager;

    private Consumer consumer;
    private Producer producer;
    private final ConcurrentMap<Short, TransactionLogSegment> segmentMap = Maps.newConcurrentMap();

    public TransactionLog(KafkaConfig config, Produce produce, Consume consume, Coordinator coordinator, ClusterManager clusterManager) {
        this.config = config;
        this.produce = produce;
        this.consume = consume;
        this.coordinator = coordinator;
        this.clusterManager = clusterManager;
    }

    @Override
    protected void validate() throws Exception {
        this.consumer = initConsumer();
        this.producer = initProducer();
    }

    protected Consumer initConsumer() {
        return new Consumer(config.getTransactionLogApp(), coordinator.getTransactionTopic().getFullName(), config.getTransactionLogApp(), Consumer.ConsumeType.INTERNAL);
    }

    protected Producer initProducer() {
        return new Producer(config.getTransactionLogApp(), coordinator.getTransactionTopic().getFullName(), config.getTransactionLogApp(), Producer.ProducerType.INTERNAL);
    }

    public boolean write(String app, String transactionId, TransactionDomain transactionDomain) throws Exception {
        TransactionLogSegment transactionLogSegment = resolveSegment(app, transactionId);
        if (transactionLogSegment == null) {
            throw new JournalqException(String.format("logSegment not exist, app: %s, transactionId: %s", app, transactionId), JournalqCode.SE_WRITE_FAILED.getCode());
        }
        byte[] body = TransactionSerializer.serialize(transactionDomain);
        return transactionLogSegment.write(app, transactionId, body);
    }

    public boolean batchWrite(String app, String transactionId, Set<? extends TransactionDomain> transactionDomains) throws Exception {
        TransactionLogSegment transactionLogSegment = resolveSegment(app, transactionId);
        if (transactionLogSegment == null) {
            throw new JournalqException(String.format("logSegment not exist, app: %s, transactionId: %s", app, transactionId), JournalqCode.SE_WRITE_FAILED.getCode());
        }
        List<byte[]> bodyList = Lists.newArrayListWithCapacity(transactionDomains.size());
        for (TransactionDomain transactionDomain : transactionDomains) {
            byte[] body = TransactionSerializer.serialize(transactionDomain);
            bodyList.add(body);
        }
        return transactionLogSegment.batchWrite(app, transactionId, bodyList);
    }

    public List<TransactionLogSegment> getSegments() {
        return Lists.newArrayList(segmentMap.values());
    }

    public TransactionLogSegment getSegment(short partition) {
        return segmentMap.get(partition);
    }

    public TransactionLogSegment removeSegment(short partition) {
        return segmentMap.remove(partition);
    }

    public List<Short> getPartitions() {
        List<Short> result = Lists.newLinkedList();
        TopicConfig topicConfig = coordinator.getTransactionTopicConfig();
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            PartitionGroup partitionGroup = entry.getValue();
            if (partitionGroup.getLeaderBroker() == null) {
                continue;
            }
            if (partitionGroup.getLeaderBroker().getId().equals(clusterManager.getBrokerId())) {
                result.addAll(partitionGroup.getPartitions());
            }
        }
        return result;
    }

    protected TransactionLogSegment resolveSegment(String app, String transactionId) {
        short partition = resolvePartition(app, transactionId);
        if (partition < 0) {
            return null;
        }
        return getOrCreateSegment(partition);
    }

    protected TransactionLogSegment getOrCreateSegment(short partition) {
        TransactionLogSegment transactionLogSegment = segmentMap.get(partition);
        if (transactionLogSegment == null) {
            transactionLogSegment = new TransactionLogSegment(config, coordinator.getTransactionTopic().getFullName(), partition, produce, consume, producer, consumer);
            TransactionLogSegment oldTransactionLogSegment = segmentMap.putIfAbsent(partition, transactionLogSegment);
            if (oldTransactionLogSegment != null) {
                transactionLogSegment = oldTransactionLogSegment;
            }
        }
        return transactionLogSegment;
    }

    // TODO hash处理
    protected short resolvePartition(String app, String transactionId) {
        TopicConfig topicConfig = coordinator.getTransactionTopicConfig();
        for (Map.Entry<Integer, PartitionGroup> entry : topicConfig.getPartitionGroups().entrySet()) {
            PartitionGroup partitionGroup = entry.getValue();
            if (partitionGroup.getLeaderBroker() == null) {
                continue;
            }
            if (partitionGroup.getLeaderBroker().getId().equals(clusterManager.getBrokerId())) {
                return partitionGroup.getPartitions().iterator().next();
            }
        }
        return -1;
    }
}