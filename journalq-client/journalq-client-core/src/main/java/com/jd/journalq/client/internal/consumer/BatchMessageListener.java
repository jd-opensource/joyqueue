package com.jd.journalq.client.internal.consumer;

import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;

import java.util.List;

/**
 * BatchMessageListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/25
 */
public interface BatchMessageListener extends BaseMessageListener {

    void onMessage(List<ConsumeMessage> messages);
}