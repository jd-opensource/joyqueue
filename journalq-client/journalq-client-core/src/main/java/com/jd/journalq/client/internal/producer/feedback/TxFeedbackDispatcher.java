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
package com.jd.journalq.client.internal.producer.feedback;

import com.jd.journalq.client.internal.cluster.ClusterManager;
import com.jd.journalq.client.internal.metadata.domain.TopicMetadata;
import com.jd.journalq.client.internal.producer.MessageSender;
import com.jd.journalq.client.internal.producer.callback.TxFeedbackCallback;
import com.jd.journalq.client.internal.producer.domain.FeedbackData;
import com.jd.journalq.client.internal.producer.domain.FetchFeedbackData;
import com.jd.journalq.client.internal.producer.domain.TransactionStatus;
import com.jd.journalq.client.internal.producer.feedback.config.TxFeedbackConfig;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JournalqCode;
import com.jd.journalq.network.command.TxStatus;
import com.jd.journalq.network.domain.BrokerNode;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TxFeedbackDispatcher
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/24
 */
public class TxFeedbackDispatcher {

    protected static final Logger logger = LoggerFactory.getLogger(TxFeedbackDispatcher.class);

    private TxFeedbackConfig config;
    private String topic;
    private TxFeedbackCallback txFeedbackCallback;
    private MessageSender messageSender;
    private ClusterManager clusterManager;

    public TxFeedbackDispatcher(TxFeedbackConfig config, String topic, TxFeedbackCallback txFeedbackCallback, MessageSender messageSender, ClusterManager clusterManager) {
        this.config = config;
        this.topic = topic;
        this.txFeedbackCallback = txFeedbackCallback;
        this.messageSender = messageSender;
        this.clusterManager = clusterManager;
    }

    public void dispatch() {
        TopicMetadata topicMetadata = clusterManager.fetchTopicMetadata(topic, config.getApp());
        if (topicMetadata == null) {
            logger.warn("topic {} not exist", topic);
            return;
        }

        for (BrokerNode brokerNode : topicMetadata.getBrokers()) {
            doFeedback(topicMetadata, brokerNode);
        }
    }

    protected void doFeedback(TopicMetadata topicMetadata, BrokerNode brokerNode) {
        FetchFeedbackData fetchFeedbackData = null;
        try {
            fetchFeedbackData = messageSender.fetchFeedback(brokerNode, topic, config.getApp(), TxStatus.UNKNOWN, config.getFetchSize(), config.getLongPollTimeout(), config.getTimeout());
            if (!fetchFeedbackData.getCode().equals(JournalqCode.SUCCESS)) {
                logger.error("fetch feedback error, topic: {}, error: {}", topic, fetchFeedbackData.getCode().getMessage());
                return;
            }
        } catch (Exception e) {
            logger.error("fetch feedback exception, topic: {}", topic, e);
            return;
        }

        if (CollectionUtils.isEmpty(fetchFeedbackData.getData())) {
            logger.debug("fetch feedback is empty, topic: {}", topic);
            return;
        }

        for (FeedbackData feedbackData : fetchFeedbackData.getData()) {
            doConfirm(brokerNode, topicMetadata, feedbackData);
        }
    }

    protected void doConfirm(BrokerNode brokerNode, TopicMetadata topicMetadata, FeedbackData feedbackData) {
        TopicName topicName = TopicName.parse(topicMetadata.getTopic());
        TransactionStatus transactionStatus = null;

        try {
            transactionStatus = txFeedbackCallback.confirm(topicName, feedbackData.getTxId(), feedbackData.getTransactionId());
            if (transactionStatus == null) {
                logger.warn("confirm feedback error, status is null, topic: {}, transactionId: {}", topic, feedbackData.getTransactionId());
                return;
            }
        } catch (Exception e) {
            logger.error("confirm feedback exception, topic: {}, transactionId: {}", topic, feedbackData.getTransactionId(), e);
            return;
        }

        try {
            if (transactionStatus.equals(TransactionStatus.PREPARE)) {
                logger.debug("commit transaction, status: {}, txId: {}, transactionId: {}", transactionStatus, feedbackData.getTxId(), feedbackData.getTransactionId());
                messageSender.commit(brokerNode, topic, config.getApp(), feedbackData.getTxId(), config.getTimeout());
            } else if (transactionStatus.equals(TransactionStatus.COMMITTED)) {
                logger.debug("rollback transaction, status: {}, txId: {}, transactionId: {}", transactionStatus, feedbackData.getTxId(), feedbackData.getTransactionId());
                messageSender.rollback(brokerNode, topic, config.getApp(), feedbackData.getTxId(), config.getTimeout());
            } else if (transactionStatus.equals(TransactionStatus.ROLLBACK)) {
                logger.debug("rollback transaction, status: {}, txId: {}, transactionId: {}", transactionStatus, feedbackData.getTxId(), feedbackData.getTransactionId());
                messageSender.rollback(brokerNode, topic, config.getApp(), feedbackData.getTxId(), config.getTimeout());
            }
        } catch (Exception e) {
            logger.error("commit feedback exception, topic: {}, transactionId: {}", topic, feedbackData.getTransactionId(), e);
        }
    }
}