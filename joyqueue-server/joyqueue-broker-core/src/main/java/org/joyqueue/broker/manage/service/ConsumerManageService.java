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

import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.monitor.PartitionAckMonitorInfo;

import java.util.List;

/**
 * ConsumerManageService
 *
 * author: gaohaoxiang
 * date: 2018/10/15
 */
public interface ConsumerManageService {

    /**
     * 设置主题下应用分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param index 索引
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setAckIndex(String topic, String app, short partition, long index) throws JoyQueueException;

    /**
     * 设置主题下应用分区的确认位置到最大
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setMaxAckIndex(String topic, String app, short partition) throws JoyQueueException;

    /**
     * 获取主题下应用的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @return 确认位置
     */
    long getAckIndex(String topic, String app, short partition);

    /**
     * 获取主题下应用所有分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @return 确认位置列表
     */
    List<PartitionAckMonitorInfo> getAckIndexes(String topic, String app);

    /**
     * 设置主题下应用所有分区的确认位置到最大
     *
     * @param topic 主题
     * @param app 应用
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setMaxAckIndexes(String topic, String app) throws JoyQueueException;

    /**
     * 根据时间设置主题下应用分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param timestamp 时间戳
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) throws JoyQueueException;

    /**
     * 根据时间获取主题下应用分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param partition 分区
     * @param timestamp 时间戳
     * @return 确认位置
     */
    long getAckIndexByTime(String topic, String app, short partition, long timestamp);

    /**
     * 根据时间获取主题下所有分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param timestamp 时间戳
     * @return 确认位置列表
     */
    List<PartitionAckMonitorInfo> getTopicAckIndexByTime(String topic, String app , long timestamp);

    /**
     * 根据时间设置主题下应用所有分区的确认位置
     *
     * @param topic 主题
     * @param app 应用
     * @param timestamp 时间戳
     * @return 是否设置成功
     * @throws JoyQueueException
     */
    boolean setAckIndexesByTime(String topic, String app, long timestamp) throws JoyQueueException;

    /**
     * 初始化消费ack
     * @param right
     * @return
     * @throws JoyQueueException
     */
    String initConsumerAckIndexes(boolean right) throws JoyQueueException;
}
