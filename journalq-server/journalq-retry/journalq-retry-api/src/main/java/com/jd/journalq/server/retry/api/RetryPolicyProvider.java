package com.jd.journalq.server.retry.api;

import com.jd.journalq.domain.TopicName;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.toolkit.retry.RetryPolicy;

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
     * @throws JMQException
     */
    RetryPolicy getPolicy(TopicName topic, String app) throws JMQException;
}
