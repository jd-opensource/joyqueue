package io.chubao.joyqueue.client.internal.consumer.callback;

import io.chubao.joyqueue.client.internal.consumer.domain.FetchMessageData;

/**
 * BatchFetchListener
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public interface FetchListener {

    void onMessage(FetchMessageData fetchMessageData);

    void onException(Throwable cause);
}