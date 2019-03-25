package com.jd.journalq.client.internal.producer.callback;

import com.jd.journalq.client.internal.producer.domain.ProduceMessage;
import com.jd.journalq.client.internal.producer.domain.SendResult;

import java.util.List;

/**
 * AsyncBatchProduceCallback
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/20
 */
public interface AsyncBatchProduceCallback {

    void onSuccess(List<ProduceMessage> messages, List<SendResult> result);

    void onException(List<ProduceMessage> messages, Throwable cause);
}