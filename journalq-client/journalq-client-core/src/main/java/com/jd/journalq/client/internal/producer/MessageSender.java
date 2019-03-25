package com.jd.journalq.client.internal.producer;

import com.jd.journalq.client.internal.producer.callback.AsyncBatchSendCallback;
import com.jd.journalq.client.internal.producer.callback.AsyncMultiBatchSendCallback;
import com.jd.journalq.client.internal.producer.callback.AsyncSendCallback;
import com.jd.journalq.client.internal.producer.domain.FetchFeedbackData;
import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendBatchResultData;
import com.jd.journalq.client.internal.producer.domain.SendPrepareResult;
import com.jd.journalq.client.internal.producer.domain.SendResultData;
import com.jd.journalq.domain.QosLevel;
import com.jd.journalq.exception.JMQCode;
import com.jd.journalq.network.command.TxStatus;
import com.jd.journalq.network.domain.BrokerNode;
import com.jd.journalq.toolkit.lang.LifeCycle;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * MessageSender
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface MessageSender extends LifeCycle {

    SendResultData send(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout);

    SendBatchResultData batchSend(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    void sendAsync(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout, AsyncSendCallback callback);

    void batchSendAsync(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout, AsyncBatchSendCallback callback);

    Future<SendBatchResultData> batchSendAsync(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    // oneway
    void sendOneway(BrokerNode brokerNode, String topic, String app, String txId, ProduceMessage message, QosLevel qosLevel, long produceTimeout, long timeout);

    void batchSendOneway(BrokerNode brokerNode, String topic, String app, String txId, List<ProduceMessage> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    void batchSendOneway(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    // batch
    Map<String, SendBatchResultData> batchSend(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    void batchSendAsync(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout, AsyncMultiBatchSendCallback callback);

    Future<Map<String, SendBatchResultData>> batchSendAsync(BrokerNode brokerNode, String app, String txId, Map<String, List<ProduceMessage>> messages, QosLevel qosLevel, long produceTimeout, long timeout);

    // transaction
    SendPrepareResult prepare(BrokerNode brokerNode, String topic, String app, String transactionId, long sequence, long transactionTimeout, long timeout);

    JMQCode commit(BrokerNode brokerNode, String topic, String app, String txId, long timeout);

    JMQCode rollback(BrokerNode brokerNode, String topic, String app, String txId, long timeout);

    FetchFeedbackData fetchFeedback(BrokerNode brokerNode, String topic, String app, TxStatus txStatus, int count, long longPollTimeout, long timeout);
}