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
package org.joyqueue.broker.producer;

import org.joyqueue.domain.QosLevel;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.JoyQueueLog;
import org.joyqueue.network.session.Producer;
import org.joyqueue.network.session.TransactionId;
import org.joyqueue.store.WriteResult;
import org.joyqueue.toolkit.concurrent.EventListener;

import java.util.List;

public interface Produce {

    /**
     * 负责PutMessage命令的写入
     * <p>
     * 对于不同类型的消息
     * 指定partition：如果客户端指定了partition，则该消息不是事务消息。
     * 非严格顺序消息：根据businessId计算出partiton，并且非事务消息。
     * 指定txId: 如果消息指定了txId，则该消息是事务消息，partition，由服务端在commit时，根据事务id指定(提前指定的partition无效)。
     * <p>
     * partition，isOrdered，txId三个值只能指定一个，如果指定多个，则按照优先级，只有第一个生效。
     *
     * @param producer session 中生产者
     * @param msgs     要写入的消息，如果是事务消息，则该批次的消息，必须都在同一个事务内，具有相同的txId。
     * @author lining11
     * Date: 2018/8/17
     */
    PutResult putMessage(Producer producer, List<BrokerMessage> msgs,
                         QosLevel qosLevel) throws JoyQueueException;

    PutResult putMessage(Producer producer, List<BrokerMessage> msgs,
                         QosLevel qosLevel, int timeout) throws JoyQueueException;

    /**
     * 异步写入
     *
     * @param producer    session 中生产者
     * @param msgs        要写入的消息，如果是事务消息，则该批次的消息，必须都在同一个事务内，具有相同的txId
     * @param qosLevel    服务水平
     * @throws JoyQueueException
     */
    void putMessageAsync(Producer producer, List<BrokerMessage> msgs,
                         QosLevel qosLevel, EventListener<WriteResult> eventListener) throws JoyQueueException;

    /**
     * 异步写入
     *
     * @param producer    session 中生产者
     * @param msgs        要写入的消息，如果是事务消息，则该批次的消息，必须都在同一个事务内，具有相同的txId
     * @param qosLevel    服务水平
     * @param timeout
     * @throws JoyQueueException
     */
    void putMessageAsync(Producer producer, List<BrokerMessage> msgs,
                         QosLevel qosLevel, int timeout, EventListener<WriteResult> eventListener) throws JoyQueueException;

    /**
     * @param producer 会话相关的producer，用来存储当前会话
     * @param tx       命令类型，包括prepare，commit，rollback
     * @throws JoyQueueException 处理异常。
     */
    TransactionId putTransactionMessage(Producer producer, JoyQueueLog tx) throws JoyQueueException;

    /**
     * 获取事务id
     * @param producer
     * @param txId
     * @return
     */
    TransactionId getTransaction(Producer producer, String txId);

    /**
     * 获取补偿
     * @param producer
     * @param count
     * @return
     * @throws JoyQueueException
     */
    List<TransactionId> getFeedback(Producer producer, int count) throws JoyQueueException;
}
