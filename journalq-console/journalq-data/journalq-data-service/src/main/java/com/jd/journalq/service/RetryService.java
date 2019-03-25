package com.jd.journalq.service;

import com.jd.journalq.domain.ConsumeRetry;
import com.jd.journalq.exception.JMQException;
import com.jd.journalq.model.PageResult;
import com.jd.journalq.model.QPageQuery;
import com.jd.journalq.model.query.QRetry;
import com.jd.journalq.server.retry.model.RetryMessageModel;

/**
 * Created by wangxiaofei1 on 2018/12/5.
 */
public interface RetryService {

    PageResult<ConsumeRetry> findByQuery(QPageQuery<QRetry> qRetryQPageQuery) throws JMQException;

    ConsumeRetry getDataById(Long id) throws JMQException;

    void add(RetryMessageModel retryMessageModel);

    /**
     * 恢复 根据状态修改
     * @param retry
     * @return
     */
    void recover(ConsumeRetry retry) throws Exception;

    void delete(ConsumeRetry retry) throws Exception;
}
