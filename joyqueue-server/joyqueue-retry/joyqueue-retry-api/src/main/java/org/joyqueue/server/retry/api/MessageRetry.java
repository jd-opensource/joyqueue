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

import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.server.retry.model.RetryMessageModel;
import org.joyqueue.toolkit.config.PropertySupplierAware;
import org.joyqueue.toolkit.lang.LifeCycle;

import java.util.List;

/**
 * 消息重试接口
 * <p>
 * Created by chengzhiliang on 2018/9/13.
 */
public interface MessageRetry<T> extends LifeCycle, PropertySupplierAware {

    /**
     * 增加重试
     *
     * @param retryMessageModelList 重试实例集合
     * @throws JoyQueueException 异常时抛出
     * @throws JoyQueueException code=JoyQueueCode.RETRY_TOKEN_LIMIT
     */
    void addRetry(List<RetryMessageModel> retryMessageModelList) throws JoyQueueException;

    /**
     * 更新重试消息状态到重试成功
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JoyQueueException 操作失败时
     */
    void retrySuccess(String topic, String app, T[] messageIds) throws JoyQueueException;

    /**
     * 更新重试消息状态到重试错误
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JoyQueueException 操作失败时
     */
    void retryError(String topic, String app, T[] messageIds) throws JoyQueueException;

    /**
     * 更新重试消息状态为重试过期
     *
     * @param topic      主题
     * @param app        应用
     * @param messageIds 消息
     * @throws JoyQueueException 操作失败时
     */
    void retryExpire(String topic, String app, T[] messageIds) throws JoyQueueException;

    /**
     * 查询指定主题和个数的重试消息
     * 该接口要求实现类返回非空List, 即要么返回非空List，要么抛出异常
     *
     * @param topic      主题
     * @param app        应用
     * @param count      条数
     * @param startIndex 起始ID
     */
    List<RetryMessageModel> getRetry(String topic, String app, short count, long startIndex) throws JoyQueueException;

    /**
     * 获取重试数据量
     *
     * @param topic 主题
     * @param app   应用
     * @return 重试数据量
     * @throws JoyQueueException 操作失败时
     */
    int countRetry(String topic, String app) throws JoyQueueException;

    /**
     * 设置重试策略和主题发现
     *
     * @param retryPolicyProvider 重试策略
     */
    void setRetryPolicyProvider(RetryPolicyProvider retryPolicyProvider);

}