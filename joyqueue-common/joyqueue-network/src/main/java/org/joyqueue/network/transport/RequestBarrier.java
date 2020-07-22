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
package org.joyqueue.network.transport;

import org.joyqueue.network.transport.config.TransportConfig;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.toolkit.concurrent.NamedThreadFactory;
import org.joyqueue.toolkit.network.IpUtil;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * RequestBarrier
 * Created by hexiaofeng on 16-6-23.
 */
public class RequestBarrier {

    protected static Logger logger = LoggerFactory.getLogger(RequestBarrier.class);

    private TransportConfig config;
    // 单向信号量
    private Semaphore onewaySemaphore;
    // 异步信号量
    private Semaphore asyncSemaphore;
    // 存放同步和异步命令应答
    private Map<Integer, ResponseFuture> futures = new ConcurrentHashMap<Integer, ResponseFuture>(200);
    // 清理时钟
    private AtomicReference<Timer> clearTimer = new AtomicReference<>();
    private ExecutorService asyncThreadPool;

    public RequestBarrier(TransportConfig config) {
        this.config = config;
        this.onewaySemaphore = config.getMaxOneway() > 0 ? new Semaphore(config.getMaxOneway(), true) : null;
        this.asyncSemaphore = config.getMaxAsync() > 0 ? new Semaphore(config.getMaxAsync(), true) : null;
        this.asyncThreadPool = Executors.newFixedThreadPool(config.getCallbackThreads(), new NamedThreadFactory("joyqueue-async-callback"));
    }

    /**
     * 获取发送超时
     *
     * @return 发送超时
     */
    public int getSendTimeout() {
        return config.getSendTimeout();
    }

    /**
     * 获取异步调用
     *
     * @param requestId 请求ID
     * @return 异步调用
     */
    public ResponseFuture get(final int requestId) {
        return futures.get(requestId);
    }

    /**
     * 缓存异步调用
     *
     * @param requestId 请求ID
     * @param future    异步调用
     */
    public void putAsyncFuture(final int requestId, final ResponseFuture future) {
        futures.put(requestId, future);
        startClearTimerIfNecessary();
    }

    /**
     * 缓存异步调用
     *
     * @param requestId 请求ID
     * @param future    异步调用
     */
    public void putSyncFuture(final int requestId, final ResponseFuture future) {
        futures.put(requestId, future);
    }

    protected void startClearTimerIfNecessary() {
        if (this.clearTimer.get() != null) {
            return;
        }
        Timer clearTimer = new Timer("joyqueue-barrier-clear-timer");
        if (this.clearTimer.compareAndSet(null, clearTimer)) {
            clearTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    evict();
                }
            }, config.getClearInterval(), config.getClearInterval());
        } else {
            clearTimer.cancel();
        }
    }

    /**
     * 移除异步调用
     *
     * @param requestId 请求ID
     * @return 异步调用
     */
    public ResponseFuture remove(final int requestId) {
        return futures.remove(requestId);
    }

    /**
     * 清理所有超时的请求
     */
    public void evict() {
        Iterator<Map.Entry<Integer, ResponseFuture>> iterator = futures.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ResponseFuture> entry = iterator.next();
            ResponseFuture future = entry.getValue();
            long timeout = future.getBeginTime() + future.getTimeout() + config.getClearInterval();

            if (timeout <= SystemClock.now() && future.getResponse() == null) {
                iterator.remove();
                if (future.release()) {
                    try {
                        future.onFailed(TransportException.RequestTimeoutException
                                .build(IpUtil.toAddress(future.getTransport().remoteAddress())));
                    } catch (Throwable e) {
                        logger.error("clear timeout response exception", e);
                    }
                }
                logger.info("remove timeout request id={} begin={} timeout={}", future.getRequestId(),
                        future.getBeginTime(), timeout);
            }
        }
    }

    /**
     * 释放所有的异步调用
     */
    public void clear() {
        ResponseFuture future;
        for (Map.Entry<Integer, ResponseFuture> entry : futures.entrySet()) {
            future = entry.getValue();
            if (future.release()) {
                try {
                    future.onFailed(TransportException.RequestTimeoutException
                            .build(IpUtil.toAddress(future.getTransport().remoteAddress())));
                } catch (Throwable ignored) {
                }
            }
        }
        futures.clear();
        if (clearTimer.get() != null) {
            clearTimer.get().cancel();
            clearTimer.set(null);
        }
        asyncThreadPool.shutdown();
    }

    /**
     * 获取信号量
     *
     * @param type    信号量类型
     * @param timeout 超时
     * @throws TransportException 传输异常时抛出
     */
    public void acquire(final SemaphoreType type, final long timeout) throws TransportException {
        if (type == null) {
            return;
        }
        Semaphore semaphore = type == SemaphoreType.ASYNC ? asyncSemaphore : onewaySemaphore;
        try {
            // 防止异步请求过多
            boolean acquire = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
            // 未获取到信号， 证明请求线程比较多
            if (!acquire) {
                throw TransportException.RequestExcessiveException.build();
            }

        } catch (InterruptedException e) {
            throw TransportException.InterruptedException.build();
        }
    }

    /**
     * 释放信号量
     *
     * @param type 类型
     */
    public void release(SemaphoreType type) {
        if (type == null) {
            return;
        }
        Semaphore semaphore = type == SemaphoreType.ASYNC ? asyncSemaphore : onewaySemaphore;
        semaphore.release();
    }

    public void onAsyncFuture(ResponseFuture future) {
        asyncThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    future.onSuccess();
                } catch (Throwable e) {
                    logger.error("execute callback error.", e);
                } finally {
                    future.release();
                }
            }
        });
    }

    public TransportConfig getConfig() {
        return config;
    }

    /**
     * 信号量类型
     */
    public enum SemaphoreType {
        /**
         * 异步
         */
        ASYNC,
        /**
         * 单向
         */
        ONEWAY
    }

}