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
package org.joyqueue.broker.consumer;

import org.joyqueue.broker.buffer.Serializer;
import org.joyqueue.domain.Consumer;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengzhiliang on 2019/2/20.
 */
public class DelayHandler {

    private final Logger logger = LoggerFactory.getLogger(DelayHandler.class);

    /**
     * 处理延迟消费
     *
     * @param consumerPolicy
     * @param byteBufferList
     * @return
     */
    public List<ByteBuffer> handle(Consumer.ConsumerPolicy consumerPolicy, List<ByteBuffer> byteBufferList) {
        if (needDelay(consumerPolicy)) {
            return filterLayerMsg(consumerPolicy, byteBufferList);
        }
        return byteBufferList;
    }

    /**
     * 判断是否配置延迟消费
     *
     * @param consumerPolicy 消费策略
     * @return 是否需要延迟消费
     */
    public boolean needDelay(Consumer.ConsumerPolicy consumerPolicy) {
        return consumerPolicy == null ? false : consumerPolicy.getDelay() > 0;
    }

    /**
     * 过滤未到消费时间的消息
     *
     * @param consumerPolicy 消费者信息
     * @param msgs           消费消息
     * @return 消息列表
     */
    private List<ByteBuffer> filterLayerMsg(Consumer.ConsumerPolicy consumerPolicy, List<ByteBuffer> msgs) {
        int delayTime = consumerPolicy.getDelay();

        List<ByteBuffer> collect = new LinkedList<>();
        for (ByteBuffer msg : msgs) {
            if (isExpire(msg, delayTime)) {
                collect.add(msg);
            } else {
                break;
            }
        }
        return collect;
    }

    /**
     * 判断是否到期
     *
     * @param msg 消息字节缓存
     * @return 到期返回true, 没有到期返回false
     */
    private boolean isExpire(ByteBuffer msg, int delayTime) {
        boolean rst = false;
        try {
            //TODO 是否需要读取存储时间
            long sendTime = Serializer.readSendTime(msg);
            long expire = sendTime + delayTime;

            // 过期时间小于等于当前时间,表示已经到期
            rst = (expire <= SystemClock.now());
        } catch (Exception e) {
            logger.warn("", e);
        }
        return rst;
    }


}
