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
package org.joyqueue.broker.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ByteBuf高性能缓冲池，基于链表实现，如果池中无可用对象，则新建缓冲区.
 * 性能是原有基于数组的缓冲池的2倍
 */
public class ByteBufPool {
    // 池化缓冲区数组
    private ArrayBlockingQueue<ByteBuf> byteBufs;
    // 缓冲区大小
    private int bufferSize;
    // 创建计数器
    private AtomicLong created = new AtomicLong(0);

    public ByteBufPool(int capacity, int bufferSize) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        }
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize must be greater than 0");
        }
        this.byteBufs = new ArrayBlockingQueue<ByteBuf>(capacity);
        this.bufferSize = bufferSize;
        for (int i = 0; i < capacity; i++) {
            byteBufs.add(newByteBuf());
            created.incrementAndGet();
        }

    }

    /**
     * 创建新缓冲区
     *
     * @return 新建的缓冲区
     */
    protected ByteBuf newByteBuf() {
        return UnpooledByteBufAllocator.DEFAULT.heapBuffer(bufferSize);
    }

    /**
     * 从池中获取缓冲区，没用则创建
     *
     * @return 缓冲区
     */
    public ByteBuf get() {
        ByteBuf buf = byteBufs.poll();
        if (buf == null) {
            buf = newByteBuf();
            created.incrementAndGet();
        }
        return buf;
    }

    /**
     * 释放缓冲区
     *
     * @param buf 缓冲区
     */
    public void release(final ByteBuf buf) {
        if (buf != null) {
            byteBufs.offer(buf);
        }
    }

    public long getCreated() {
        return created.get();
    }
}
