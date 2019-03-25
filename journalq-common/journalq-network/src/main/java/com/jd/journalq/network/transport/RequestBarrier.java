package com.jd.journalq.network.transport;

import com.jd.journalq.network.transport.config.TransportConfig;
import com.jd.journalq.network.transport.exception.TransportException;
import com.jd.journalq.toolkit.network.IpUtil;
import com.jd.journalq.toolkit.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 请求并发控制
 * Created by hexiaofeng on 16-6-23.
 */
public class RequestBarrier {

    protected static Logger logger = LoggerFactory.getLogger(RequestBarrier.class);

    private TransportConfig config;
    // 单向信号量
    public Semaphore onewaySemaphore;
    // 异步信号量
    public Semaphore asyncSemaphore;
    // 存放同步和异步命令应答
    public Map<Integer, ResponseFuture> futures = new ConcurrentHashMap<Integer, ResponseFuture>(200);


    public RequestBarrier(TransportConfig config) {
        this.config = config;
        this.onewaySemaphore = config.getMaxOneway() > 0 ? new Semaphore(config.getMaxOneway(), true) : null;
        this.asyncSemaphore = config.getMaxAsync() > 0 ? new Semaphore(config.getMaxAsync(), true) : null;
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
    public void put(final int requestId, final ResponseFuture future) {
        futures.put(requestId, future);
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
            long timeout = future.getBeginTime() + future.getTimeout() + 1000;

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
    }

    /**
     * 获取信号量
     *
     * @param type    信号量类型
     * @param timeout 超时
     * @throws TransportException
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