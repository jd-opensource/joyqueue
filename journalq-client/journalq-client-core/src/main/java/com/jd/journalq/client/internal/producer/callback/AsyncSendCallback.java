package com.jd.journalq.client.internal.producer.callback;

import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendResultData;

/**
 * AsyncSendCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/10
 */
public interface AsyncSendCallback {

    void onSuccess(ProduceMessage message, SendResultData result);

    void onException(ProduceMessage message, Throwable cause);
}