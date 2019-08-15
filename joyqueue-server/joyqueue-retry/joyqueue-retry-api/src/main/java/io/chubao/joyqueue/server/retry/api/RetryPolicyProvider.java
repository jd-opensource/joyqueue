package io.chubao.joyqueue.server.retry.api;

import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.toolkit.retry.RetryPolicy;

/**
 * 重试策略类
 *
 * Created by chengzhiliang on 2019/2/22.
 */
public interface RetryPolicyProvider {

    /**
     * 获取重试策略
     *
     * @param topic 主题
     * @param app 应用
     * @return
     * @throws JoyQueueException
     */
    RetryPolicy getPolicy(TopicName topic, String app) throws JoyQueueException;
}
