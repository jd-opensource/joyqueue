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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 过滤管道
 * <p>
 * Created by chengzhiliang on 2019/2/20.
 */
public class FilterPipeline<T extends MessageFilter> {
    // 基于链表的过滤管道
    private final LinkedList<T> pipeline = new LinkedList<>();
    // 管道里面是否有过滤器
    private volatile boolean hasFilter = false;
    // 管理唯一标示
    private String id;

    public FilterPipeline(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * 注册过滤器到管道
     *
     * @param t
     */
    public void register(T t) {
        pipeline.addLast(t);

        hasFilter = true;
    }

    /**
     * 执行过滤
     *
     * @param byteBuffers
     * @return
     * @throws JoyQueueException
     */
    public List<ByteBuffer> execute(List<ByteBuffer> byteBuffers, FilterCallback filterCallback) throws JoyQueueException {
        if (hasFilter) {
            Iterator<T> iterator = pipeline.iterator();
            while (iterator.hasNext()) {
                T next = iterator.next();
                byteBuffers = next.filter(byteBuffers, filterCallback);
            }
        }

        return byteBuffers;
    }

}