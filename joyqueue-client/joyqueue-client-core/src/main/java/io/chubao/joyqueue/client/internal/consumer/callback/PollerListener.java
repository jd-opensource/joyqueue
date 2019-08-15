package io.chubao.joyqueue.client.internal.consumer.callback;

import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeMessage;

import java.util.List;

/**
 * BatchFetchListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface PollerListener {

    void onMessage(List<ConsumeMessage> messages);

    void onException(Throwable cause);
}