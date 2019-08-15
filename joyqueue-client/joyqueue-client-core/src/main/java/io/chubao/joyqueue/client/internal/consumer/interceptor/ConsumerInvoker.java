package io.chubao.joyqueue.client.internal.consumer.interceptor;

import io.chubao.joyqueue.client.internal.consumer.domain.ConsumeReply;

import java.util.List;

/**
 * ConsumerInvoker
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2019/1/11
 */
public interface ConsumerInvoker {

    List<ConsumeReply> invoke(ConsumeContext context);

    List<ConsumeReply> reject(ConsumeContext context);
}