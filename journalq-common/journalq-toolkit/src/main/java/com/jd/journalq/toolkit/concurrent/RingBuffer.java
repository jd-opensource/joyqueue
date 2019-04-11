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
package com.jd.journalq.toolkit.concurrent;

import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.os.Systems;
import com.jd.journalq.toolkit.time.SystemClock;
import sun.misc.Unsafe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 无锁高性能环形缓冲区，适用于多个生产者和一个消费者
 * CPU cache line大小为64字节
 * Created by hexiaofeng on 16-6-29.
 */
public class RingBuffer<E> implements LifeCycle {
    protected static final Long FALSE = 0L;
    protected static final Long TRUE = 1L;
    protected static final int BUFFER_PAD;
    protected static final int OBJECT_ARRAY_ELEMENT_SIZE;
    protected static final long REF_ARRAY_BASE;
    protected static final int REF_ELEMENT_SHIFT;
    protected static final long WRITER_OFFSET;
    protected static final long READER_OFFSET;
    protected static final long STARTED_OFFSET;
    protected static final Unsafe UNSAFE = Systems.getUnsafe();
    protected static final long INITIAL_POSITION = -1L;

    // 掩码，用于计算在数组中的下标
    protected long p1, p2, p3, p4, p5, p6, p7;
    protected final long mask;
    protected long p11, p12, p13, p14, p15, p16, p17;

    // 缓冲器
    protected final Object[] entries;

    // 缓冲器容量，能存储的最大记录数，改成Long型和后面7位占位符组成一个Cache Line
    protected long p21, p22, p23, p24, p25, p26, p27;
    protected final long capacity;
    protected long p31, p32, p33, p34, p35, p36, p37;

    // 写位置
    protected long p41, p42, p43, p44, p45, p46, p47;
    protected volatile long writer = INITIAL_POSITION;
    protected long p51, p52, p53, p54, p55, p56, p57;

    // 读位置
    protected long p61, p62, p63, p64, p65, p66, p67;
    protected volatile long reader = INITIAL_POSITION;
    protected long p71, p72, p73, p74, p75, p76, p77;

    // 开始标示
    protected long p81, p82, p83, p84, p85, p86, p87;
    protected volatile long started = FALSE;
    protected long p91, p92, p93, p94, p95, p96, p97;

    // 锁
    protected final ReentrantLock lock = new ReentrantLock();
    // 没用空间写入数据条件
    protected final Condition writeSignal = lock.newCondition();
    // 有数据条件
    protected final Condition readSignal = lock.newCondition();
    // 等待处理完
    protected final Condition doneSignal = lock.newCondition();
    // 派发线程
    protected Thread thread;
    // 消费者
    protected Consumer consumer;
    // 名称
    protected String name;
    // 监听
    protected EventHandler handler;

    static {
        try {
            WRITER_OFFSET = UNSAFE.objectFieldOffset(RingBuffer.class.getDeclaredField("writer"));
            READER_OFFSET = UNSAFE.objectFieldOffset(RingBuffer.class.getDeclaredField("reader"));
            STARTED_OFFSET = UNSAFE.objectFieldOffset(RingBuffer.class.getDeclaredField("started"));
            OBJECT_ARRAY_ELEMENT_SIZE = UNSAFE.arrayIndexScale(Object[].class);
            if (4 == OBJECT_ARRAY_ELEMENT_SIZE) {
                REF_ELEMENT_SHIFT = 2;
            } else if (8 == OBJECT_ARRAY_ELEMENT_SIZE) {
                REF_ELEMENT_SHIFT = 3;
            } else {
                throw new IllegalStateException("Unknown pointer size");
            }
            BUFFER_PAD = 128 / OBJECT_ARRAY_ELEMENT_SIZE;
            // Including the buffer pad in the array base offset
            REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class) + (BUFFER_PAD << REF_ELEMENT_SHIFT);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public RingBuffer(final int capacity, final EventHandler handler) {
        this(capacity, handler, null);
    }

    public RingBuffer(final int capacity, final EventHandler handler, final String name) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be greater than 0");
        } else if (handler == null) {
            throw new IllegalArgumentException("handler can not be null");
        }

        // 容量是2的指数
        int size = 1;
        while (capacity > size) {
            size <<= 1;
        }
        this.capacity = size;
        this.mask = size - 1;
        // 前后都加BUFFER_PAD占位符
        this.entries = new Object[size + 2 * BUFFER_PAD];
        this.handler = handler;
        this.name = name;
    }

    @Override
    public void start() throws Exception {
        if (UNSAFE.compareAndSwapLong(this, STARTED_OFFSET, FALSE, TRUE)) {
            UNSAFE.putOrderedLong(this, WRITER_OFFSET, INITIAL_POSITION);
            UNSAFE.putOrderedLong(this, READER_OFFSET, INITIAL_POSITION);
            consumer = new Consumer(handler, doneSignal);
            thread = new Thread(consumer, name == null ? this.getClass().getSimpleName() : name);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void stop() {
        stop(0L);
    }

    /**
     * 停止，如果数据没用处理完，则等待一段时间直到完成或超时
     *
     * @param timeout 等待数据处理完的超时时间
     *                <li>>0 等待超时时间</li>
     *                <li>=0 等待处理完</li>
     *                <li><0 立即返回</li>
     */
    public void stop(long timeout) {
        if (UNSAFE.compareAndSwapLong(this, STARTED_OFFSET, TRUE, FALSE)) {
            lock.lock();
            try {
                readSignal.signalAll();
                writeSignal.signalAll();
                // 在锁里面停止，确保能正确通知doneSignal信号
                Consumer consumer = this.consumer;
                if (consumer != null && consumer.isStarted()) {
                    consumer.stop();
                    // 等等数据处理完
                    if (timeout == 0) {
                        doneSignal.await();
                    } else if (timeout > 0) {
                        doneSignal.await(timeout, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                // 当前线程终止
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean isStarted() {
        return started == TRUE;
    }

    /**
     * 添加数据
     *
     * @param element 数据
     * @return 成功标示
     */
    public boolean add(final E element) {
        return add(element, 0);
    }

    /**
     * 添加数据
     *
     * @param element 数据
     * @param timeout 超时时间
     *                <li>>0 等待超时时间</li>
     *                <li>=0 无限等待</li>
     *                <li>&lt;0 没有空间立即返回</li>
     * @return 成功标示
     */
    public boolean add(final E element, final long timeout) {
        long curWrite, size;
        long start = SystemClock.now();
        long time = Locks.tryLock(lock, timeout);
        if (time == Locks.LOCK_FAIL) {
            return false;
        }
        try {
            while (started == TRUE) {
                curWrite = writer + 1;
                size = curWrite - reader;
                if (size > 0 && size <= capacity) {
                    // 有空间
                    UNSAFE.putOrderedLong(this, WRITER_OFFSET, curWrite);
                    // 保存数据
                    UNSAFE.putObject(entries, REF_ARRAY_BASE + ((curWrite & mask) << REF_ELEMENT_SHIFT), element);
                    // 通知读取线程
                    readSignal.signalAll();
                    return true;
                    // 并发写入
                } else if (timeout < 0) {
                    // 没有空间立即返回
                    return false;
                } else if (timeout == 0) {
                    if (!Locks.awaitQuiet(writeSignal)) {
                        return false;
                    }
                    // 无限等等
                } else if (timeout > 0) {
                    // 有超时时间
                    time = timeout - (SystemClock.now() - start);
                    if (time < 0) {
                        // 超时了
                        return false;
                    }
                    if (!Locks.awaitQuiet(writeSignal, time)) {
                        return false;
                    }
                }
            }
            return false;
        } finally {
            lock.unlock();
        }

    }

    /**
     * 获取数据，单线程读取
     *
     * @param maxSize 最大条数
     *                <li>>0 获取的最大条数</li>
     *                <li>=0 有多少返回多少</li>
     *                <li>&lt;0 必须满足其绝对值数量</li>
     * @param timeout 超时时间
     *                <li>>0 不满足数据条数则等待指定超时时间</li>
     *                <li>=0 不满足数据条数则无限等待</li>
     *                <li>&lt;0 不满足数据条数则立即返回</li>
     * @return 数据列表
     */
    protected Object[] get(final int maxSize, final long timeout) {
        long preRead, curRead;
        Object[] result = null;
        long start = SystemClock.now();
        int size, offset, srcPos, remain;
        int max = maxSize;
        if (max < 0) {
            max = -max;
            // 必须满足数量的时候，不能超过缓冲器大小
            if (max > capacity) {
                max = (int) capacity;
            }
        }
        long time = Locks.tryLock(lock, timeout);
        if (time == Locks.LOCK_FAIL) {
            return new Object[0];
        }
        try {
            while (isStarted()) {
                // 获取前面读序号
                preRead = reader;
                // 获取当前读序号
                curRead = preRead + 1;
                // 判断能读取的数据条数
                size = (int) (writer - preRead);
                if (size <= 0 && timeout >= 0 || size > 0 && maxSize < 0 && size < max && timeout >= 0) {
                    // 没有数据并且需要等待，或者不能满足需要的条数并且需要等待
                    if (timeout == 0) {
                        // 永久等待
                        if (!Locks.awaitQuiet(readSignal)) {
                            return result;
                        }
                    } else {
                        // 有超时时间
                        time = timeout - (SystemClock.now() - start);
                        if (time < 0) {
                            // 超时了
                            return result;
                        }
                        if (!Locks.awaitQuiet(readSignal, time)) {
                            return result;
                        }
                    }
                } else {
                    // 立即返回
                    if (size <= 0) {
                        size = 0;
                    } else {
                        // max == 0 返回全部
                        size = (max == 0 || max >= size) ? size : max;
                    }
                    if (size > 0) {
                        // 拷贝数据
                        result = new Object[size];
                        // 读取数据在有效数据的偏移位置
                        offset = (int) (curRead & mask);
                        // 起始位置
                        srcPos = BUFFER_PAD + offset;
                        // 判断数组末尾还剩余多少
                        remain = (int) capacity - offset;
                        // 设置读位置
                        UNSAFE.putOrderedLong(this, READER_OFFSET, preRead + size);
                        if (remain >= size) {
                            System.arraycopy(entries, srcPos, result, 0, size);
                        } else {
                            System.arraycopy(entries, srcPos, result, 0, remain);
                            System.arraycopy(entries, BUFFER_PAD, result, remain, size - remain);
                        }
                        writeSignal.signalAll();
                    }
                    return result;
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 空间容量
     *
     * @return 空间容量
     */
    public int capacity() {
        return (int) capacity;
    }

    /**
     * 可以读取数据的大小
     *
     * @return 可以读取数据的大小
     */
    public int readable() {
        return (int) (writer - reader);
    }

    /**
     * 可以写入数据的大小
     *
     * @return 可以写入数据的大小
     */
    public int writable() {
        return (int) (capacity - (writer - reader));
    }

    /**
     * 获取元素
     *
     * @param position 位置
     * @return 元素
     */
    protected final E get(final long position) {
        return (E) UNSAFE.getObject(entries, REF_ARRAY_BASE + ((position & mask) << REF_ELEMENT_SHIFT));
    }

    /**
     * 保存元素
     *
     * @param position 位置
     * @param element  元素
     */
    protected void put(final long position, final E element) {
        UNSAFE.putObject(entries, REF_ARRAY_BASE + ((position & mask) << REF_ELEMENT_SHIFT), element);
    }

    /**
     * 缓冲区处理器
     */
    public interface EventHandler {

        /**
         * 派发数据
         *
         * @param elements 消息列表
         * @throws Exception
         */
        void onEvent(Object[] elements) throws Exception;

        /**
         * 出现异常处理
         *
         * @param exception 异常
         */
        void onException(Throwable exception);

        /**
         * 返回批量大小
         *
         * @return 批量大小
         * <li>>0 获取的最大条数</li>
         * <li>=0 不启用条数限制</li>
         * <li>&lt;0 必须满足其绝对值数量</li>
         */
        int getBatchSize();

        /**
         * 返回触发的时间间隔
         *
         * @return 时间间隔
         * <li>>0 指定时间内会触发一次事件，不管有没有数据</li>
         * <li>&lt;=0 当没有数据消费的时候，会挂住</li>
         */
        long getInterval();
    }

    /**
     * 派发任务
     */
    protected class Consumer implements Runnable {
        // 等待处理完
        protected Condition doneSignal;
        // 监听
        protected EventHandler handler;
        // 启动标示
        protected CAtomicLong started = new CAtomicLong(TRUE);

        public Consumer(EventHandler handler, Condition doneSignal) {
            this.handler = handler;
            this.doneSignal = doneSignal;
        }

        /**
         * 是否在启动
         *
         * @return
         */
        public boolean isStarted() {
            return started.get() == TRUE;
        }

        /**
         * 停止
         */
        public void stop() {
            started.setVolatile(FALSE);
        }

        @Override
        public void run() {
            Object[] elements;
            long timeout;
            while (isStarted()) {
                timeout = handler.getInterval();
                elements = get(handler.getBatchSize(), timeout);
                // 处理数据
                dispatch(elements, timeout > 0);
            }
            timeout = handler.getInterval();
            elements = get(handler.getBatchSize(), -1);
            // 处理数据
            dispatch(elements, timeout > 0);
            Locks.signalAll(lock, doneSignal);
        }

        /**
         * 处理请求,时间敏感的监听器,为空也需要触发通知
         *
         * @param elements 请求列表
         * @timeAware 时间敏感
         */
        protected void dispatch(final Object[] elements, final boolean timeAware) {
            if (!timeAware && (elements == null || elements.length == 0)) {
                return;
            }
            try {
                handler.onEvent(elements);
            } catch (Throwable e) {
                handler.onException(e);
            }
        }
    }

}
