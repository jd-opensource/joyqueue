package io.chubao.joyqueue.client.internal.consumer.callback;

import io.chubao.joyqueue.client.internal.consumer.domain.FetchMessageData;

/**
 * BatchPartitionFetchListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface PartitionConsumerListener {

    void onMessage(FetchMessageData fetchMessageData);

    void onException(String topic, short partition, long index, Throwable cause);
}