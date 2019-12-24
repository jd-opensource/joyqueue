/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.service;

import org.joyqueue.domain.ConsumeRetry;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.model.PageResult;
import org.joyqueue.model.QPageQuery;
import org.joyqueue.model.query.QRetry;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.server.retry.model.RetryQueryCondition;

/**
 * Created by wangxiaofei1 on 2018/12/5.
 */
public interface RetryService {

    PageResult<ConsumeRetry> findByQuery(QPageQuery<QRetry> qRetryQPageQuery) throws JoyQueueException;

    ConsumeRetry getDataById(Long id,String topic) throws JoyQueueException;

    void add(RetryMessageModel retryMessageModel);

    /**
     * 恢复 根据状态修改
     * @param retry
     * @return
     */
    void recover(ConsumeRetry retry) throws Exception;

    void delete(ConsumeRetry retry) throws Exception;

    void batchDelete(RetryQueryCondition retryQueryCondition, Long updateTime, int updateBy) throws Exception;

    /**
     * 重试服务是否可用
     * @throws Exception
     */
    boolean isServerEnabled();
}
