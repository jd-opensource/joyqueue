package com.jd.journalq.client.internal.consumer.callback;

import com.google.common.collect.Table;
import com.jd.journalq.client.internal.consumer.domain.FetchMessageData;

/**
 * BatchPartitionFetchListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface BatchPartitionFetchListener {

    void onMessage(Table<String, Short, FetchMessageData> fetchMessageTable);

    void onException(Throwable cause);
}