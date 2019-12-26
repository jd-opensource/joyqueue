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

import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.CommandCallback;
import org.joyqueue.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ResponseFuture
 */
public class ResponseFuture implements Future {
    protected static Logger logger = LoggerFactory.getLogger(ResponseFuture.class);
    // 开始事件
    private final long beginTime = SystemClock.now();
    // 请求
    private Command request;
    // 应答
    private Command response;
    // 异常
    private Throwable cause;
    // 超时
    private long timeout;
    // 请求ID
    private int requestId;
    // 通道
    private Transport transport;
    // 回调
    private CommandCallback callback;
    // 是否成功
    private boolean success;
    // 回调一次
    private AtomicBoolean onceCallback = new AtomicBoolean(false);
    // 是否释放
    private AtomicBoolean released = new AtomicBoolean(false);
    // 门闩
    private CountDownLatch latch;
    // barrier
    private RequestBarrier requestBarrier;
    // 信号量类型
    private RequestBarrier.SemaphoreType semaphoreType;
    // 是否完成
    private volatile boolean isDone = false;
    // 是否取消
    private volatile boolean isCancel = false;

    /**
     * 异步调用构造函数
     *
     * @param transport 通道
     * @param request   请求
     * @param timeout   超时
     * @param callback  异步调用回调
     * @param requestBarrier barrier
     * @param semaphoreType 信号量类型
     * @param latch     门闩
     */
    public ResponseFuture(Transport transport, Command request, long timeout, CommandCallback callback,
                          RequestBarrier requestBarrier, RequestBarrier.SemaphoreType semaphoreType, CountDownLatch latch) {
        if (request == null) {
            throw new IllegalArgumentException("request can not be null");
        }
        this.transport = transport;
        this.request = request;
        if (request != null && request.getHeader() != null) {
            this.requestId = request.getHeader().getRequestId();
        }
        this.timeout = timeout;
        this.callback = callback;
        this.requestBarrier = requestBarrier;
        this.semaphoreType = semaphoreType;
        this.latch = latch;
    }

    public Command getRequest() {
        return request;
    }

    public Command getResponse() {
        return this.response;
    }

    public void setResponse(Command response) {
        this.response = response;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public long getTimeout() {
        return timeout;
    }

    public int getRequestId() {
        return requestId;
    }

    public CommandCallback getCallback() {
        return this.callback;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Transport getTransport() {
        return transport;
    }

    /**
     * 是否超时
     *
     * @return
     */
    public boolean isTimeout() {
        return SystemClock.getInstance().now() > beginTime + timeout;
    }

    /**
     * 阻塞并等待返回
     *
     * @return 应答命令
     * @throws InterruptedException 被打断时抛出
     */
    public Command await() throws InterruptedException {
        if (latch != null) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        }
        return response;
    }

    /**
     * 等待返回
     *
     * @param timeout 超时时间
     * @return 应答命令
     * @throws InterruptedException 被打断时抛出
     */
    public Command await(long timeout) throws InterruptedException {
        if (latch != null) {
            latch.await(timeout, TimeUnit.MILLISECONDS);
        }
        return response;
    }

    /**
     * 回调
     */
    public void callback() {
        if (callback == null) {
            return;
        }
        if (onceCallback.compareAndSet(false, true)) {
            try {
                if (isSuccess()) {
                    callback.onSuccess(request, response);
                } else if (cause != null) {
                    callback.onException(request, cause);
                } else {
                    logger.error("bigbug: success and exception confused! {}", request);
                }
            } catch (Throwable ignored) {
                logger.error("callback error", ignored);
            }
        }
    }

    /**
     * 确定成功
     */
    public void onSuccess() {
        setSuccess(true);
        callback();
    }

    /**
     * 确定失败
     * @param cause 异常
     */
    public void onFailed(Throwable cause) {
        setSuccess(false);
        setCause(cause);
        callback();
    }

    /**
     * 释放资源，不回调
     *
     * @return 成功标示
     */
    public boolean release() {
        return release(null, false);
    }

    public boolean released() {
        return released.get();
    }

    /**
     * 释放资源，并回调
     *
     * @param e        异常
     * @param callback 是否回调
     * @return 成功标示
     */
    public boolean release(final Throwable e, final boolean callback) {
        // 确保释放一次
        if (released.compareAndSet(false, true)) {
            // 设置了异常，则不成功
            if (e != null) {
                success = false;
                cause = e;
            }
            // 释放请求资源
            if (request != null) {
                request.release();
            }
            // 释放信号量
            if (semaphoreType != null) {
                requestBarrier.release(semaphoreType);
            }
            // 唤醒同步等待线程
            if (latch != null) {
                latch.countDown();
            }
            // 回调
            if (callback) {
                this.callback();
            }
            // 清空资源引用
            isDone = true;

            if (!isCancel) {
                synchronized (this) {
                    notifyAll();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (isCancel) {
            return false;
        }
        isCancel = true;
        synchronized (this) {
            notifyAll();
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return isCancel;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        if (isDone) {
            return getFutureResponse();
        }
        synchronized (this) {
            wait();
        }
        if (isCancel) {
            throw new InterruptedException();
        }
        return getFutureResponse();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (isDone()) {
            return getFutureResponse();
        }
        synchronized (this) {
            wait(unit.toMillis(timeout));
        }
        if (isCancel) {
            throw new InterruptedException();
        }
        if (!isDone) {
            throw new TimeoutException();
        }
        return getFutureResponse();
    }

    protected Object getFutureResponse() throws ExecutionException {
        if (cause != null) {
            throw new ExecutionException(cause);
        }
        return response;
    }
}