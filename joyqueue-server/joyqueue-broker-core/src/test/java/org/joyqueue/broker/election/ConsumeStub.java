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
package org.joyqueue.broker.election;

import org.joyqueue.broker.consumer.Consume;
import org.joyqueue.broker.consumer.model.ConsumePartition;
import org.joyqueue.broker.consumer.model.PullResult;
import org.joyqueue.broker.consumer.position.model.Position;
import org.joyqueue.domain.TopicName;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.MessageLocation;
import org.joyqueue.network.session.Connection;
import org.joyqueue.network.session.Consumer;

import java.util.Map;


/**
 * Created by zhuduohui on 2018/10/18.
 */
public class ConsumeStub implements Consume {

    /**
     * 获取消息
     *
     * @param consumer   从会话管理中获取中的消费者对象，进程中全局唯一
     * @param count      获取消息条数
     * @param ackTimeout 占用partition的超时时间，单位毫秒
     * @return
     * @throws JoyQueueException
     */
    @Override
    public PullResult getMessage(Consumer consumer, int count, int ackTimeout) throws JoyQueueException{
        return null;
    }

    /**
     * 获取消息，kafka使用，不走应答逻辑，只关注pullIndex
     *
     * @param consumer  从会话管理中获取中的消费者对象
     * @param partition 默认值0
     * @param index     默认值-1
     * @param count     获取消息条数
     * @return
     * @throws JoyQueueException
     */
    @Override
    public PullResult getMessage(Consumer consumer, short partition, long index, int count) throws JoyQueueException {
        return null;
    }

    @Override
    public PullResult getMessage(String topic, short partition, long index, int count) {
        return null;
    }

    /**
     * 应答消息，不包含重试消息
     *
     * @param locations    应答消息
     * @param consumer     消费者
     * @param isSuccessAck 是否正常确认
     * @return 是否成功
     * @throws JoyQueueException
     */
    @Override
    public boolean acknowledge(MessageLocation[] locations, Consumer consumer, Connection connection, boolean isSuccessAck) throws JoyQueueException {
        return true;
    }

    /**
     * 是否还有空闲分区
     *
     * @param consumer 消费者
     * @return 是否有空闲分区
     */
    @Override
    public boolean hasFreePartition(Consumer consumer) {
        return true;
    }

    /**
     * 重置指定主题+应用+分区的拉取序号
     *
     * @param consumer 消费者
     * @param index    消息序号
     */
    @Override
    public void setPullIndex(Consumer consumer, short partition, long index) {}

    /**
     * 获取指定主题+应用+分区的拉取序号
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    @Override
    public long getPullIndex(Consumer consumer, short partition) {
        return 0;
    }

    /**
     * 重置指定主题+应用+分区的应答序号
     *
     * @param consumer 消费者
     * @param index    消息序号
     */
    @Override
    public void setAckIndex(Consumer consumer, short partition, long index) {}

    /**
     * 获取指定主题+应用+分区的应答序号
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    @Override
    public long getAckIndex(Consumer consumer, short partition) {
        return 0;
    }

    /**
     * 重置指定主题+应用到最新生产的位置
     *
     * @param topic 消息主题
     * @param app   应用
     * @return
     */
    @Override
    public boolean resetPullIndex(String topic, String app) {
        return true;
    }

    @Override
    public boolean setConsumePosition(Map<ConsumePartition, Position>  consumeInfoJson) {
        return false;
    }

    @Override
    public Map<ConsumePartition, Position> getConsumePositionByGroup(TopicName topic, String app, int partitionGroup) {
        return null;
    }

    @Override
    public Map<ConsumePartition, Position> getConsumePositionByGroup(TopicName topic, int partitionGroup) {
        return null;
    }

    /**
     * 添加消费者
     *
     * @param topic 消费主题
     * @param app   应用
     */

    public void addConsumer(String topic, String app) {}

    /**
     * 移除消费者
     *
     * @param topic 消费主题
     * @param app   应用
     */
    public void removeConsumer(String topic, String app) {}

    @Override
    public long getMinIndex(Consumer consumer, short partition) {
        return 0;
    }

    @Override
    public long getMaxIndex(Consumer consumer, short partition) {
        return 0;
    }

    /**
     * 重置指定主题+应用+分区的应答序号（起始订阅位置）
     *
     * @param consumer 消费者
     * @param index    消息序号
     */
    @Override
    public void setStartAckIndex(Consumer consumer, short partition, long index){}


    /**
     * 获取指定主题+应用+分区的应答序号（起始订阅位置）
     *
     * @param consumer  消费者
     * @param partition 分区
     * @return 消息序号
     */
    @Override
    public long getStartIndex(Consumer consumer, short partition) {return 0L;}

    @Override
    public void releasePartition(String topic, String app, short partition) {

    }
}
