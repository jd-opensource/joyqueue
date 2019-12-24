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
package org.joyqueue.broker.consumer.filter;

import org.joyqueue.exception.JoyQueueException;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * 消息过滤接口
 * <p>
 * Created by chengzhiliang on 2019/2/20.
 */
public interface MessageFilter {

    /**
     * 消息过滤
     *
     * @param byteBufferList 消息字节缓存集合
     * @param filterCallback 消息过滤后的回调
     * @return
     * @throws JoyQueueException
     */
    List<ByteBuffer> filter(List<ByteBuffer> byteBufferList, FilterCallback filterCallback) throws JoyQueueException;

    /**
     * 设置过滤规则
     *
     * @param rule 过滤规则
     */
    void setRule(String rule);

}
