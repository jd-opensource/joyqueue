package io.chubao.joyqueue.server.retry.api;

import io.chubao.joyqueue.domain.ConsumeRetry;
import io.chubao.joyqueue.exception.JoyQueueException;
import io.chubao.joyqueue.model.PageResult;
import io.chubao.joyqueue.server.retry.model.RetryQueryCondition;
import io.chubao.joyqueue.server.retry.model.RetryStatus;

/**
 * Created by chengzhiliang on 2019/2/20.
 */
public interface ConsoleMessageRetry<T> extends MessageRetry<T> {

    PageResult<ConsumeRetry> queryConsumeRetryList(RetryQueryCondition retryQueryCondition) throws JoyQueueException;

    ConsumeRetry getConsumeRetryById(Long id) throws JoyQueueException;

    void updateStatus(String topic, String app, T[] messageId, RetryStatus status, long updateTime, int updateBy) throws Exception;

}
