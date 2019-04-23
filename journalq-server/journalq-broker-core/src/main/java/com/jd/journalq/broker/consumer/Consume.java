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
package com.jd.journalq.broker.consumer;

import com.jd.journalq.broker.consumer.model.PullResult;
import com.jd.journalq.domain.TopicName;
import com.jd.journalq.network.session.Connection;
import com.jd.journalq.network.session.Consumer;
import com.jd.journalq.exception.JournalqException;
import com.jd.journalq.message.MessageLocation;

/**
 * Created by chengzhiliang on 2018/8/16.
 */
public interface Consume {

    /**
     * 获取消息
     *
     * @param consumer   从会话管理中获取中的消费者对象，进程中全局唯一
     * @param count      获取消息条数
     * @param ackTimeout 占用partition的超时时间，单位毫秒
     * @return
     * @throws JournalqException
     */
    PullResult getMessage(Consumer consumer, int count, int ackTimeout) throws JournalqException;

    /**
     * 指定分区、消息序号、获取消息条数获取消息
     *
     * @param consumer  从会话管理中获取中的消费者对象
     * @param partition 默认值0
     * @param index     默认值-1
     * @param count     获取消息条数
     * @return
     * @throws JournalqException
     */
    PullResult getMessage(Consumer consumer, short partition, long index, int count) throws JournalqException;

    /**
     * 应答消息，不包含重试消息
     *
     * @param locations    应答消息
     * @param consumer     消费者
     * @param isSuccessAck 是否正常确认
     * @return 是否成功
     * @throws JournalqException
     */
    boolean acknowledge(MessageLocation[] locations, Consumer consumer, Connection connection, boolean isSuccessAck) throws JournalqException;


    /**
     * 是否还有空闲分区
     *
     * @param consumer 消费者
     * @return 是否有空闲分区
     */
    boolean hasFreePartition(Consumer consumer);

    /**
     * 重置指定主题+应用+分区的拉取序号
     *
     * @param consumer 消费者
     * @param index    消息序号
     */
    void setPullIndex(Consumer consumer, short partition, long index) throws JournalqException;

    /**
     * 获取指定主题+应用+分区的拉取序号
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    long getPullIndex(Consumer consumer, short partition);

    /**
     * 重置指定主题+应用+分区的应答序号
     *
     * @param consumer 消费者
     * @param index    消息序号
     */
    void setAckIndex(Consumer consumer, short partition, long index) throws JournalqException;

    /**
     * 重置指定主题+应用+分区的应答序号（起始订阅位置）
     *
     * @param consumer 消费者
     * @param index    消息序号
     */
    void setStartAckIndex(Consumer consumer, short partition, long index) throws JournalqException;

    /**
     * 获取指定主题+应用+分区的应答序号
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    long getAckIndex(Consumer consumer, short partition);

    /**
     * 获取指定主题+应用+分区的应答序号（起始订阅位置）
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    long getStartIndex(Consumer consumer, short partition);

    /**
     * 重置指定主题+应用到最新生产的位置
     *
     * @param topic 消息主题
     * @param app   应用
     * @return
     */
    boolean resetPullIndex(String topic, String app) throws JournalqException;

    /**
     * 保存消息位置
     *
     * @param consumeInfoJson 消息位置信息json字符串
     * @return
     */
    boolean setConsumeInfo(String consumeInfoJson);

    /**
     * 获取指定主题+应用+分组的消费位置
     *
     * @param topic          消费主题
     * @param app            应用
     * @param partitionGroup 分区分组
     */
    String getConsumeInfoByGroup(TopicName topic, String app, int partitionGroup);

    /**
     * 获取指定分区最小的消息序号
     *
     * @param consumer
     * @param partition
     * @return
     */
    long getMinIndex(Consumer consumer, short partition);

    /**
     * 获取指定分区最大的消息序号
     *
     * @param consumer
     * @param partition
     * @return
     */
    long getMaxIndex(Consumer consumer, short partition);

    /**
     * 获取最近一次拉取消息时间
     *
     * @param topic
     * @param app
     * @param partition
     * @return
     */
    long getLastPullTimeByPartition(TopicName topic, String app, short partition);

    /**
     * 获取最近一次应答消息时间
     * @param topic
     * @param app
     * @param partition
     * @return
     */
    long getLastAckTimeByPartition(TopicName topic, String app, short partition);

}
