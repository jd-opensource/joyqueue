package com.jd.journalq.client.internal.producer.callback;

import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendBatchResultData;

import java.util.List;

/**
 * AsyncBatchSendCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public interface AsyncBatchSendCallback {

    void onSuccess(List<ProduceMessage> messages, SendBatchResultData result);

    void onException(List<ProduceMessage> messages, Throwable cause);
}