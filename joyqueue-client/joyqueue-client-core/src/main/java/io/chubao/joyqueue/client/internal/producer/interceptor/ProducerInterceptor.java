package io.chubao.joyqueue.client.internal.producer.interceptor;

import io.chubao.joyqueue.client.internal.common.interceptor.BaseInterceptor;
import io.chubao.joyqueue.client.internal.producer.domain.SendResult;

import java.util.List;

/**
 * ProducerInterceptor
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public interface ProducerInterceptor extends BaseInterceptor {

    boolean preSend(ProduceContext context);

    void postSend(ProduceContext context, List<SendResult> result);
}