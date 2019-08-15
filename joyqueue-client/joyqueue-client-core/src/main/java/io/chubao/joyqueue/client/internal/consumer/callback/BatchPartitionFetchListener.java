package io.chubao.joyqueue.client.internal.consumer.callback;

import com.google.common.collect.Table;
import io.chubao.joyqueue.client.internal.consumer.domain.FetchMessageData;

/**
 * BatchPartitionFetchListener
 *
 * author: gaohaoxiang
 * date: 2018/12/10
 */
public interface BatchPartitionFetchListener {

    void onMessage(Table<String, Short, FetchMessageData> fetchMessageTable);

    void onException(Throwable cause);
}