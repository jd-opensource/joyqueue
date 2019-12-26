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

import org.joyqueue.toolkit.lang.LifeCycle;
import org.joyqueue.toolkit.time.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 事件管理器，事件顺序执行
 *
 * @author hexiaofeng
 * @since 2013-12-09
 */
public class EventBus<E> implements LifeCycle {
    // 监听器
    protected CopyOnWriteArrayList<EventListener<E>> listeners = new CopyOnWriteArrayList<EventListener<E>>();
    // 事件队里
    protected BlockingQueue<Ownership> events;
    // 线程名称
    protected String name;
    // 事件派发处理器
    protected EventDispatcher dispatcher;
    // 启动标示
    protected AtomicBoolean started = new AtomicBoolean(false);
    // 事件的间隔（毫秒），并合并事件
    protected long interval;
    // 触发空闲事件的时间
    protected long idleTime;
    // 获取事件超时时间
    protected long timeout = 1000;

    public EventBus() {
        this(null, 0);
    }

    public EventBus(EventListener<E> listener) {
        this(null, listener, 0);
    }

    public EventBus(String name) {
        this(name, 0);
    }

    public EventBus(String name, int capacity) {
        this.name = name;
        if (capacity > 0) {
            events = new ArrayBlockingQueue<Ownership>(capacity);
        } else {
            events = new LinkedBlockingDeque<Ownership>();
        }
    }

    public EventBus(String name, EventListener<E> listener) {
        this(name, 0);
        addListener(listener);
    }

    public EventBus(String name, EventListener<E> listener, int capacity) {
        this(name, capacity);
        addListener(listener);
    }

    public EventBus(String name, List<? extends EventListener<E>> listeners) {
        this(name, 0);
        if (listeners != null) {
            for (EventListener<E> listener : listeners) {
                addListener(listener);
            }
        }
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        if (timeout > 0) {
            this.timeout = timeout;
        }
    }

    /**
     * 开始
     */
    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            // 清理一下，防止里面有数据
            events.clear();
            // 每次重新创建
            dispatcher = new EventDispatcher();
            Thread thread;
            if (name != null) {
                thread = new Thread(dispatcher, name);
            } else {
                thread = new Thread(dispatcher);
            }
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * 结束
     */
    public void stop() {
        stop(false);
    }

    /**
     * 结束
     *
     * @param gracefully 优雅停止
     */
    public void stop(boolean gracefully) {
        if (started.compareAndSet(true, false)) {
            if (dispatcher != null) {
                dispatcher.stop(gracefully);
                events.clear();
            }
        }
    }

    /**
     * 是否启动
     *
     * @return 启动标示
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * 增加监听器
     *
     * @param listener
     */
    public boolean addListener(final EventListener<E> listener) {
        if (listener != null) {
            return listeners.addIfAbsent(listener);
        }
        return false;
    }

    /**
     * 删除监听器
     *
     * @param listener
     */
    public boolean removeListener(final EventListener<E> listener) {
        if (listener != null) {
            return listeners.remove(listener);
        }
        return false;
    }

    public List<EventListener<E>> getListeners() {
        return listeners;
    }


    /**
     * 发布事件到队列中，持续等待队列有空间
     *
     * @param event 事件
     */
    public boolean add(final E event) {
        return add(event, null);
    }

    /**
     * 发布事件到队列中，当队列没有空间的时候，会等待指定的时间
     *
     * @param event   事件
     * @param timeout 超时
     * @param unit    时间单位
     */
    public boolean add(final E event, final long timeout, final TimeUnit unit) {
        return add(event, null, timeout, unit);
    }

    /**
     * 发布事件到队列中，持续等待队列有空间
     *
     * @param event 事件
     * @param owner 所有者
     */
    public boolean add(final E event, final EventListener<E> owner) {
        if (event == null) {
            return false;
        }
        try {
            events.put(new Ownership(event, owner));
            return true;
        } catch (InterruptedException e) {
            // 让当前线程中断
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 发布事件到队列中，持续等待队列有空间
     *
     * @param event   事件
     * @param owner   所有者
     * @param timeout 超时
     * @param unit    时间单位
     */
    public boolean add(final E event, final EventListener<E> owner, final long timeout, final TimeUnit unit) {
        if (event == null) {
            return false;
        }
        try {
            return events.offer(new Ownership(event, owner), timeout, unit);
        } catch (InterruptedException e) {
            // 让当前线程中断
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 同步通知事件
     *
     * @param event 事件
     */
    public void inform(final E event) {
        if (event == null) {
            return;
        }
        Throwable throwable = null;
        for (EventListener<E> listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Throwable t) {
                throwable = t;
            }
        }

        if (throwable != null) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * 空闲事件
     */
    protected void onIdle() {

    }

    /**
     * 合并事件
     *
     * @param events 事件列表
     */
    protected void publish(final List<Ownership> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        // 发布最后一个事件
        publish(events.get(events.size() - 1));
    }

    /**
     * 派发消息
     *
     * @param event 事件
     */
    protected void publish(final Ownership event) {
        // 当triggerNoEvent为真时候，event可以为空
        if (event != null && event.owner != null) {
            try {
                event.owner.onEvent(event.event);
            } catch (Throwable ignored) {
            }
        } else {
            E e = event == null ? null : event.event;
            for (EventListener<E> listener : listeners) {
                if (e != null || (listener instanceof EventListener.Heartbeat) && (((EventListener.Heartbeat) listener)
                        .trigger(SystemClock.now()))) {
                    // 心跳感知，判断是否要触发
                    try {
                        listener.onEvent(e);
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    /**
     * 事件
     */
    protected class Ownership {
        public E event;
        public EventListener<E> owner;

        public Ownership(E event, EventListener<E> owner) {
            this.event = event;
            this.owner = owner;
        }
    }

    /**
     * 事件派发
     */
    protected class EventDispatcher implements Runnable {

        private CountDownLatch latch = new CountDownLatch(1);
        private AtomicReference<State> state = new AtomicReference<State>(State.STARTED);

        /**
         * 关闭
         *
         * @param gracefully 是否优雅关闭
         */
        public void stop(final boolean gracefully) {
            state.set(gracefully ? State.STOPPED_GRACEFULLY : State.STOPPED);
            if (gracefully) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // 让当前线程中断
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public void run() {
            long lastTime = SystemClock.now();
            long now;
            Ownership event;
            boolean sleeping;
            State status;
            while (true) {
                try {
                    sleeping = interval > 0;
                    event = null;
                    // 判断是否关闭
                    status = state.get();
                    if (status == State.STARTED) {
                        // 没有关闭，则获取数据
                        event = events.poll(timeout, TimeUnit.MILLISECONDS);
                        // 再取一下，因为可能阻塞
                        status = state.get();
                    }
                    if (status == State.STOPPED) {
                        // 强制关闭
                        break;
                    } else if (status == State.STOPPED_GRACEFULLY) {
                        // 优雅关闭，如果当前事件为空，则重新取一次
                        if (event == null) {
                            event = events.poll();
                        }
                    }
                    if (event == null && status == State.STOPPED_GRACEFULLY) {
                        // 没有事件，优雅退出
                        break;
                    } else if (event != null) {
                        // 当前事件不为空
                        if (idleTime > 0) {
                            // 启用空闲检测
                            lastTime = SystemClock.now();
                        }
                        // 合并事件
                        if (interval > 0) {
                            // 获取当前所有事件
                            List<Ownership> currents = new ArrayList<Ownership>();
                            currents.add(event);
                            while (!events.isEmpty()) {
                                event = events.poll();
                                if (event != null) {
                                    currents.add(event);
                                }
                            }
                            // 合并事件
                            publish(currents);
                        } else {
                            publish(event);
                        }
                    } else {
                        // 没有获取到消息，有些监听器有心跳需求
                        publish((Ownership) null);
                        if (idleTime > 0) {
                            // 启用空闲检测
                            now = SystemClock.now();
                            if (now - lastTime > idleTime) {
                                lastTime = now;
                                // 如果这个时候又有新消息了，则需要尽快处理，不需要再次休眠
                                if (!events.isEmpty()) {
                                    sleeping = false;
                                } else {
                                    // 空闲处理
                                    onIdle();
                                }
                            }
                        }
                    }
                    if (sleeping && status == State.STARTED) {
                        // 休息间隔时间
                        Thread.sleep(interval);
                    }
                } catch (InterruptedException e) {
                    // 让当前线程中断
                    Thread.currentThread().interrupt();
                } catch (Throwable ignored) {
                    // 忽略异常
                }
            }
            if (latch != null) {
                latch.countDown();
            }

        }
    }

    /**
     * 排放器状态
     */
    protected enum State {
        /**
         * 启动
         */
        STARTED,
        /**
         * 终止
         */
        STOPPED,
        /**
         * 优雅终止
         */
        STOPPED_GRACEFULLY
    }

}


