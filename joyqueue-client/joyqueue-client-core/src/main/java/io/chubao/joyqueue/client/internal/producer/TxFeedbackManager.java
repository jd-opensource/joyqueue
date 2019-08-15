package io.chubao.joyqueue.client.internal.producer;

import io.chubao.joyqueue.client.internal.producer.callback.TxFeedbackCallback;
import io.chubao.joyqueue.toolkit.lang.LifeCycle;

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