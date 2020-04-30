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
package org.joyqueue.server.retry.api;

import org.joyqueue.domain.ConsumeRetry;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.model.PageResult;
import org.joyqueue.server.retry.model.*;

import java.util.List;

/**
 * Created by chengzhiliang on 2019/2/20.
 */
public interface ConsoleMessageRetry<T> extends MessageRetry<T> {

    PageResult<ConsumeRetry> queryConsumeRetryList(RetryQueryCondition retryQueryCondition) throws JoyQueueException;

    ConsumeRetry getConsumeRetryById(Long id,String topic) throws JoyQueueException;

    void updateStatus(String topic, String app, T[] messageId, RetryStatus status, long updateTime, int updateBy) throws Exception;

    void batchUpdateStatus(RetryQueryCondition retryQueryCondition, RetryStatus status, long updateTime, int updateBy) throws Exception;

    /**
     *
     * Physical delete retry message by topic,app ,the status and update time before  expire time
     * @param topic  topic
     * @param app  app
     * @param status {@link RetryStatus}
     * @param expireTimeStamp  clean before the expire time
     * @return  affect rows
     *
     **/
    int cleanBefore(String topic,String app,int status,long expireTimeStamp) throws Exception;

    /**
     *
     * Top N topic and app by condition, if topics undefine, include all topics
     *
     * @param status  query condition
     * @param N  top N
     *
     **/
    List<RetryMonitorItem> top(int N,int status) throws Exception;

    /**
     *  All consumers on retry
     *
     **/
    List<RetryMonitorItem> allConsumer() throws Exception;

}
