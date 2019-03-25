package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;

/**
 * MessageListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/25
 */
public interface MessageListener extends BaseMessageListener {

    void onMessage(ConsumeMessage message);
}