package com.jd.journalq.client.internal.consumer.callback;

import com.jd.journalq.client.internal.consumer.domain.FetchMessageData;

/**
 * BatchFetchListener
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface FetchListener {

    void onMessage(FetchMessageData fetchMessageData);

    void onException(Throwable cause);
}