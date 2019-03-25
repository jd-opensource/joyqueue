package com.jd.journalq.toolkit.service;

import com.jd.journalq.toolkit.exception.Abnormity;
import com.jd.journalq.toolkit.lang.Online;

/**
 * 服务线程，伴随服务生命周期，定时间隔运行
 */
public class ServiceThread implements Runnable, Abnormity, Online {
    public static final long SLEEP_ON_ERROR = 2000L;
    // 容器
    protected Service parent;
    // 轮询时间间隔
    protected long interval;
    // 出错的时候休息时间
    protected long sleepOnError = SLEEP_ON_ERROR;

    public ServiceThread(Service parent) {
        this(parent, 0, SLEEP_ON_ERROR);
    }

    public ServiceThread(Service parent, long interval) {
        this(parent, interval, SLEEP_ON_ERROR);
    }

    public ServiceThread(Service parent, long interval, long sleepOnError) {
        if (parent == null) {
            throw new IllegalArgumentException("parent can not be null");
        }
        this.parent = parent;
        this.interval = interval;
        this.sleepOnError = sleepOnError;
    }

    @Override
    public boolean onException(Throwable e) {
        return true;
    }

    @Override
    public boolean isStarted() {
        return parent.isStarted() && !Thread.currentThread().isInterrupted();
    }

    public long getInterval() {
        return interval;
    }

    /**
     * 执行任务
     *
     * @throws Exception
     */
    protected void execute() throws Exception {

    }

    /**
     * 等待时间
     *
     * @param time
     */
    protected void await(final long time) {
        if (time > 0) {
            parent.await(time);
        }
    }

    /**
     * 线程终止
     */
    protected void stop() {

    }

    @Override
    public void run() {
        long interval = this.interval;
        while (isStarted()) {
            try {
                interval = getInterval();
                if (interval > 0) {
                    // 休息一段时间
                    await(interval);
                    // 再次判断是否存活
                    if (!isStarted()) {
                        break;
                    }
                }
                // 处理业务
                execute();
            } catch (InterruptedException e) {
                // 出现中断异常
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                // 出现异常
                if (!onException(e)) {
                    break;
                } else if (interval <= 0 && sleepOnError > 0) {
                    // 避免无限循环
                    await(sleepOnError);
                }
            }
        }
        // 退出
        stop();
    }
}
