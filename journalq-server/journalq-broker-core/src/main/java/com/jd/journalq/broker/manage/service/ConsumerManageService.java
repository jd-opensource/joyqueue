/**
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
package com.jd.journalq.broker.manage.service;

import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.monitor.PartitionAckMonitorInfo;

import java.util.List;

/**
 * ConsumerManageService
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/10/15
 */
public interface ConsumerManageService {

    /**
     * 设置ackindex
     *
     * @param topic
     * @param app
     * @param partition
     * @param index
     * @return
     */
    boolean setAckIndex(String topic, String app, short partition, long index) throws JournalqException;

    /**
     * 设置最大ack索引
     *
     * @param topic
     * @param app
     * @return
     */
    boolean setMaxAckIndex(String topic, String app, short partition) throws JournalqException;

    /**
     * 返回最大ack索引
     *
     * @param topic
     * @param app
     * @return
     */
    long getAckIndex(String topic, String app, short partition);

    /**
     * 返回最大ack索引
     *
     * @param topic
     * @param app
     * @return
     */
    List<PartitionAckMonitorInfo> getAckIndexes(String topic, String app);

    /**
     * 设置最大ack索引
     *
     * @param topic
     * @param app
     * @return
     */
    boolean setMaxAckIndexes(String topic, String app) throws JournalqException;

    /**
     * 根据时间设置ack
     *
     * @param topic
     * @param app
     * @param partition
     * @param timestamp
     * @return
     */
    boolean setAckIndexByTime(String topic, String app, short partition, long timestamp) throws JournalqException;

    /**
     * 根据时间返回ack
     *
     * @param topic
     * @param app
     * @param partition
     * @param timestamp
     * @return
     */
    long getAckIndexByTime(String topic, String app, short partition, long timestamp);

    /**
     * 根据时间返回ack
     *
     * @param topic
     * @param app
     * @param timestamp
     * @return
     */
    List<PartitionAckMonitorInfo> getTopicAckIndexByTime(String topic, String app , long timestamp);

    /**
     * 根据时间设置ack
     *
     * @param topic
     * @param app
     * @param timestamp
     * @return
     */
    boolean setAckIndexesByTime(String topic, String app, long timestamp) throws JournalqException;
}
