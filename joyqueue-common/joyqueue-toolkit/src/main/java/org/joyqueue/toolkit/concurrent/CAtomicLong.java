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
package org.joyqueue.toolkit.concurrent;

import org.joyqueue.toolkit.os.Systems;
import sun.misc.Unsafe;

/**
 * 位置，可以充分利用CPU缓存，加快访问速度
 * Created by hexiaofeng on 16-6-29.
 */
public class CAtomicLong {
    private static final Unsafe UNSAFE;
    private static final long VALUE_OFFSET;
    // 占位符，CPU cache line大小为64字节
    protected long p1, p2, p3, p4, p5, p6, p7;
    protected volatile long value;
    // 占位符，CPU cache line大小为64字节
    protected long p9, p10, p11, p12, p13, p14, p15;

    static {
        UNSAFE = Systems.getUnsafe();
        try {
            VALUE_OFFSET = UNSAFE.objectFieldOffset(CAtomicLong.class.getDeclaredField("value"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CAtomicLong() {
        this(-1L);
    }

    public CAtomicLong(long value) {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, value);
    }

    /**
     * 获取当前值
     *
     * @return
     */
    public long get() {
        return value;
    }

    /**
     * 设置当前值
     *
     * @param value
     */
    public void set(final long value) {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, value);
    }

    /**
     * 设置当前值，确保其它线程读取到新的值
     *
     * @param value
     */
    public void setVolatile(final long value) {
        UNSAFE.putLongVolatile(this, VALUE_OFFSET, value);
    }

    /**
     * 比较更新
     *
     * @param expect   比较的值
     * @param position 设置的目标值
     * @return 成功标示
     */
    public boolean compareAndSet(final long expect, final long position) {
        return UNSAFE.compareAndSwapLong(this, VALUE_OFFSET, expect, position);
    }

    /**
     * 递增并获取增加后的值
     *
     * @return 增加后的值
     */
    public long incrementAndGet() {
        return addAndGet(1);
    }

    /**
     * 增加并获取增加后的值
     *
     * @param increment
     * @return 增加后的值
     */
    public long addAndGet(final long increment) {
        long current;
        long target;

        do {
            current = get();
            target = current + increment;
        } while (!compareAndSet(current, target));
        return target;
    }
}