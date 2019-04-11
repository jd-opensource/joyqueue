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
package com.jd.journalq.client.internal.producer.support;

import com.google.common.collect.Lists;
import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.metadata.domain.PartitionMetadata;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.nameserver.NameServerConfig;
import com.jd.journalq.client.internal.producer.MessageSender;
import com.jd.journalq.client.internal.producer.TransactionMessageProducer;
import com.jd.journalq.client.internal.producer.checker.ProduceMessageChecker;
import com.jd.journalq.client.internal.producer.config.ProducerConfig;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendPrepareResult;
import com.jd.journalq.client.internal.producer.domain.SendResult;
import com.jd.journalq.client.internal.producer.exception.ProducerException;
import com.jd.journalq.client.internal.producer.helper.ProducerHelper;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.toolkit.lang.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DefaultTransactionMessageProducer
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public class DefaultTransactionMessageProducer implements TransactionMessageProducer {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultTransactionMessageProducer.class);

    private String transactionId;
    private long timeout;
    private TimeUnit timeoutUnit;
    private long sequence;

    private ProducerConfig config;
    private NameServerConfig nameServerConfig;
    private ClusterManager clusterManager;
    private MessageSender messageSender;
    private MessageProducerInner messageProducerInner;

    private PartitionMetadata transactionPartition;
    private SendPrepareResult prepare;
    private JMQCode commit;
    private JMQCode rollback;

    public DefaultTransactionMessageProducer(String transactionId, long timeout, TimeUnit timeoutUnit, long sequence, ProducerConfig config, NameServerConfig nameServerConfig, ClusterManager clusterManager, MessageSender messageSender, MessageProducerInner messageProducerInner) {
        Preconditions.checkArgument(config != null, "config not null");
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");
        Preconditions.checkArgument(nameServerConfig != null, "nameServer not null");
        Preconditions.checkArgument(clusterManager != null, "clusterManager not null");
        Preconditions.checkArgument(messageSender != null, "messageSender not null");
        Preconditions.checkArgument(messageProducerInner != null, "messageProducerInner not null");

        this.transactionId = transactionId;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.sequence = sequence;
        this.config = config;
        this.nameServerConfig = nameServerConfig;
        this.clusterManager = clusterManager;
        this.messageSender = messageSender;
        this.messageProducerInner = messageProducerInner;
    }

    public synchronized void commit() {
        checkPrepare();
        checkState();

        JMQCode commit = messageSender.commit(transactionPartition.getLeader(), transactionPartition.getTopic(), config.getApp(), prepare.getTxId(), config.getTimeout());
        if (!commit.equals(JMQCode.SUCCESS)) {
            throw new ProducerException(commit.getMessage(), commit.getCode());
        }
        this.commit = commit;
    }

    @Override
    public synchronized void rollback() {
        checkPrepare();
        checkState();

        JMQCode rollback = messageSender.rollback(transactionPartition.getLeader(), transactionPartition.getTopic(), config.getApp(), prepare.getTxId(), config.getTimeout());
        if (!rollback.equals(JMQCode.SUCCESS)) {
            throw new ProducerException(rollback.getMessage(), rollback.getCode());
        }
        this.rollback = rollback;
    }

    @Override
    public SendResult send(ProduceMessage message) {
        return send(message, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public SendResult send(ProduceMessage message, long timeout, TimeUnit timeoutUnit) {
        List<SendResult> sendResults = batchSend(Lists.newArrayList(message), timeout, timeoutUnit);
        if (CollectionUtils.isEmpty(sendResults)) {
            return null;
        }
        return sendResults.get(0);
    }

    @Override
    public List<SendResult> batchSend(List<ProduceMessage> messages) {
        return batchSend(messages, config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    @Override
    public List<SendResult> batchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        return doBatchSend(messages, timeout, timeoutUnit);
    }

    public synchronized List<SendResult> doBatchSend(List<ProduceMessage> messages, long timeout, TimeUnit timeoutUnit) {
        checkState();
        ProduceMessageChecker.checkMessages(messages, config);
        Preconditions.checkArgument(timeoutUnit != null, "timeoutUnit not null");

        TopicMetadata topicMetadata = messageProducerInner.checkTopicMetadata(messages.get(0).getTopic());

        if (prepare == null) {
            List<BrokerNode> brokers = messageProducerInner.getRegionBrokers(topicMetadata);
            brokers = messageProducerInner.filterNotAvailableBrokers(brokers);
            List<PartitionMetadata> partitions = messageProducerInner.getBrokerPartitions(topicMetadata, brokers);
            transactionPartition = messageProducerInner.dispatchPartitions(messages, topicMetadata, partitions, null);
            prepare = doPrepare(transactionPartition);
        } else {
            Preconditions.checkArgument(messages.get(0).getTopic().equals(transactionPartition.getTopic()), "transaction message must be single partition");
            ProducerHelper.setPartitions(messages, transactionPartition.getId());
        }

        return messageProducerInner.doBatchSend(messages, topicMetadata, transactionPartition, null, prepare.getTxId(), timeout, timeoutUnit, false, false, null);
    }

    protected SendPrepareResult doPrepare(PartitionMetadata partition) {
        SendPrepareResult sendPrepareResult = messageSender.prepare(partition.getLeader(), partition.getTopic(), config.getApp(), transactionId, sequence, timeoutUnit.toMillis(timeout), config.getTimeout());
        if (!sendPrepareResult.getCode().equals(JMQCode.SUCCESS)) {
            throw new ProducerException(sendPrepareResult.getCode().getMessage(), sendPrepareResult.getCode().getCode());
        }
        return sendPrepareResult;
    }

    protected void checkPrepare() {
        if (prepare == null) {
            throw new ProducerException("transaction is not beginning", JMQCode.FW_TRANSACTION_EXISTS.getCode());
        }
    }

    protected void checkState() {
        if (commit != null || rollback != null) {
            throw new ProducerException("transaction is not beginning", JMQCode.FW_TRANSACTION_EXISTS.getCode());
        }
    }
}