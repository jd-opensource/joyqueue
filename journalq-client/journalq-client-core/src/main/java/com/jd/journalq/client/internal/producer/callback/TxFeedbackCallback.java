package com.jd.journalq.client.internal.producer.callback;

import com.jd.journalq.client.internal.producer.domain.TransactionStatus;
import com.jd.journalq.domain.TopicName;

/**
 * TxFeedbackCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface TxFeedbackCallback {

    TransactionStatus confirm(TopicName topic, String txId, String transactionId);
}