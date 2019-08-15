package io.chubao.joyqueue.client.internal.consumer;

import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;

import java.util.List;

/**
 * BatchMessageListener
 *
 * author: gaohaoxiang
 * date: 2018/12/25
 */
public interface BatchMessageListener extends BaseMessageListener {

    void onMessage(List<ConsumeMessage> messages);
}