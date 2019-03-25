package com.jd.journalq.client.internal.producer.interceptor;

import com.jd.journalq.client.internal.producer.domain.SendResult;

import java.util.List;

/**
 * ProducerInvoker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public interface ProducerInvoker {

    List<SendResult> invoke(ProduceContext context);

    List<SendResult> reject(ProduceContext context);
}