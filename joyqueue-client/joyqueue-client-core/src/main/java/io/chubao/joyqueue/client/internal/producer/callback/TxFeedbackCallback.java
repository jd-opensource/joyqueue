package io.chubao.joyqueue.client.internal.producer.callback;

import io.chubao.joyqueue.client.internal.producer.domain.TransactionStatus;
import io.chubao.joyqueue.domain.TopicName;

/**
 * TxFeedbackCallback
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public interface TxFeedbackCallback {

    TransactionStatus confirm(TopicName topic, String txId, String transactionId);
}