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
package org.joyqueue.broker.manage.service;

import org.joyqueue.monitor.BrokerMessageInfo;

import java.util.List;

/**
 * MessageManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public interface MessageManageService {

    /**
     * 获取主题下应用分区的消息
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param index 索引
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> getPartitionMessage(String topic, String app, short partition, long index, int count);

    /**
     * 获取主题下应用的积压消息
     *
     * @param topic 主题
     * @param app 应用
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> getPendingMessage(String topic, String app, int count);

    /**
     * 获取主题下应用的最新消息
     *
     * @param topic 主题
     * @param app 应用
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> getLastMessage(String topic, String app, int count);

    /**
     * 获取主题下应用的消息，如果有积压返回积压，否则返回最新几条
     *
     * @param topic 主题
     * @param app 应用
     * @param count 数量
     * @return 消息列表
     */
    List<BrokerMessageInfo> viewMessage(String topic, String app, int count);
}