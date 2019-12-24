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
package org.joyqueue.toolkit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 服务
 */
public abstract class Activity {
    private final Logger logger = LoggerFactory.getLogger(Activity.class);
    // 锁
    protected final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    // 锁
    protected final Lock readLock = rwLock.readLock();
    // 锁
    protected final Lock writeLock = rwLock.writeLock();
    // 是否启动
    protected final AtomicBoolean started = new AtomicBoolean(false);
    // 服务状态
    protected final AtomicReference<ServiceState> serviceState = new AtomicReference<ServiceState>();
    // 信号量
    protected final Object signal = new Object();

    /**
     * 启动
     * @throws Exception
     */
    protected void start() throws Exception {
        validate();
        serviceState.set(ServiceState.WILL_START);
        beforeStart();
        writeLock.lock();
        try {
            if (started.compareAndSet(false, true)) {
                try {
                    serviceState.set(ServiceState.STARTING);
                    doStart();
                    afterStart();
                    serviceState.set(ServiceState.STARTED);
                } catch (Exception e) {
                    serviceState.set(ServiceState.START_FAILED);
                    startError(e);
                    stop();
                    // 应对一些场景，需要转换一下异常
                    Exception ex = convert(e);
                    if (ex != null) {
                        throw ex;
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }

    }

    /**
     * 启动前
     *
     * @throws Exception
     */
    protected void beforeStart() throws Exception {

    }

    /**
     * 验证
     *
     * @throws Exception
     */
    protected void validate() throws Exception {

    }

    /**
     * 启动
     *
     * @throws Exception
     */
    protected void doStart() throws Exception {

    }

    /**
     * 启动后
     *
     * @throws Exception
     */
    protected void afterStart() throws Exception {

    }

    /**
     * 启动出错
     *
     * @param e 异常
     */
    protected void startError(Exception e) {
        logger.error("start error ",e);
    }

    /**
     * 转换异常
     *
     * @param e 原异常
     * @return 目标异常
     */
    protected Exception convert(final Exception e) {
        return e;
    }

    protected void stop() {
        stop(null);
    }

    /**
     * 停止
     *
     * @param runnable 运行
     */
    protected void stop(final Runnable runnable) {
        // 设置状态将要关闭
        serviceState.set(ServiceState.WILL_STOP);
        synchronized (signal) {
            // 通知等待线程，即将关闭
            signal.notifyAll();
        }
        beforeStop();
        writeLock.lock();
        try {
            if (started.compareAndSet(true, false)) {
                serviceState.set(ServiceState.STOPPING);
                doStop();
                afterStop();
                if (runnable != null) {
                    runnable.run();
                }
                serviceState.set(ServiceState.STOPPED);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 停止前
     */
    protected void beforeStop() {

    }

    /**
     * 停止
     */
    protected void doStop() {

    }

    /**
     * 停止后
     */
    protected void afterStop() {

    }

    /**
     * 将要关闭
     */
    protected void willStop() {
        serviceState.set(ServiceState.WILL_STOP);
        beforeStop();
    }

    protected ServiceState getServiceState() {
        return serviceState.get();
    }

    /**
     * 是否启动
     * @return
     */
    protected boolean isStarted() {
        if (started.get()) {
            switch (serviceState.get()) {
                case WILL_STOP:
                    return false;
                case STOPPING:
                    return false;
                case STOPPED:
                    return false;
                default:
                    return true;
            }
        }
        return false;
    }

    /**
     * 是否关闭状态，包括启动失败，即将关闭，关闭中，关闭完成
     *
     * @return 关闭状态标示
     */
    protected boolean isStopped() {
        switch (serviceState.get()) {
            case START_FAILED:
                return true;
            case WILL_STOP:
                return true;
            case STOPPING:
                return true;
            case STOPPED:
                return true;
            default:
                return false;
        }
    }

    /**
     * 等待一段时间，如果服务已经关闭则立即返回
     *
     * @param time 时间
     */
    protected void await(final long time) {
        if (!isStarted()) {
            return;
        }
        synchronized (signal) {
            try {
                signal.wait(time);
            } catch (InterruptedException e) {
                // 当前线程终止
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 是否就绪
     *
     * @return 就绪标示
     */
    protected boolean isReady() {
        return serviceState.get() == ServiceState.STARTED;
    }

    /**
     * 获取写锁
     *
     * @return 写锁
     */
    protected Lock getWriteLock() {
        return writeLock;
    }

    /**
     * 获取读锁
     *
     * @return 读锁
     */
    protected Lock getReadLock() {
        return rwLock.readLock();
    }

    /**
     * 服务状态
     */
    public enum ServiceState {
        /**
         * 准备启动
         */
        WILL_START, /**
         * 启动中
         */
        STARTING, /**
         * 启动失败
         */
        START_FAILED, /**
         * 启动完成
         */
        STARTED, /**
         * 准备关闭
         */
        WILL_STOP, /**
         * 关闭中
         */
        STOPPING, /**
         * 关闭完成
         */
        STOPPED
    }

}
