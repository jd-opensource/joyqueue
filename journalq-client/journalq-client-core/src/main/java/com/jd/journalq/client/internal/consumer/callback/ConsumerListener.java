package com.jd.journalq.client.internal.consumer.callback;

import com.jd.journalq.client.internal.consumer.domain.ConsumeMessage;

import java.util.List;

/**
 * BatchFetchListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface ConsumerListener {

    void onMessage(List<ConsumeMessage> messages);

    void onException(Throwable cause);
}