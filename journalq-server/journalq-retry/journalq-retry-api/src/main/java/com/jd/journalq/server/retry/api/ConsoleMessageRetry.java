package com.jd.journalq.server.retry.api;

import com.jd.journalq.common.domain.ConsumeRetry;
import com.jd.journalq.common.exception.JMQException;
import com.jd.journalq.common.model.PageResult;
import com.jd.journalq.server.retry.model.RetryQueryCondition;
import com.jd.journalq.server.retry.model.RetryStatus;

/**
 * Created by chengzhiliang on 2019/2/20.
 */
public interface ConsoleMessageRetry<T> extends MessageRetry<T> {

    PageResult<ConsumeRetry> queryConsumeRetryList(RetryQueryCondition retryQueryCondition) throws JMQException;

    ConsumeRetry getConsumeRetryById(Long id) throws JMQException;

    void updateStatus(String topic, String app, T[] messageId, RetryStatus status, long updateTime, int updateBy) throws Exception;

}
