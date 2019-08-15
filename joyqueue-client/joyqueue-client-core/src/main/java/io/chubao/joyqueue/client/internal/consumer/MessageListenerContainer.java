package io.chubao.joyqueue.client.internal.consumer;

import io.chubao.joyqueue.toolkit.lang.LifeCycle;

/**
 * MessageListenerContainer
 *
 * author: gaohaoxiang
 * date: 2018/12/25
 */
public interface MessageListenerContainer extends LifeCycle {

    void addListener(String topic, MessageListener messageListener);

    void addBatchListener(String topic, BatchMessageListener messageListener);
}