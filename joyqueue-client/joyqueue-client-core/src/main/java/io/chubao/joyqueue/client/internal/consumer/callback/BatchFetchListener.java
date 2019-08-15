package io.chubao.joyqueue.client.internal.consumer.callback;

import io.chubao.joyqueue.client.internal.consumer.domain.FetchMessageData;

import java.util.Map;

/**
 * BatchFetchListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface BatchFetchListener {

    void onMessage(Map<String, FetchMessageData> fetchMessageMap);

    void onException(Throwable cause);
}