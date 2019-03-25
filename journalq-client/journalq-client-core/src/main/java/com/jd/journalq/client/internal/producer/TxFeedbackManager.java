package com.jd.journalq.client.internal.producer;

import com.jd.journalq.client.internal.producer.callback.TxFeedbackCallback;
import com.jd.journalq.toolkit.lang.LifeCycle;

/**
 * TxFeedbackManager
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/3
 */
public interface TxFeedbackManager extends LifeCycle {

    void setTransactionCallback(String topic, TxFeedbackCallback callback);

    void removeTransactionCallback(String topic);
}