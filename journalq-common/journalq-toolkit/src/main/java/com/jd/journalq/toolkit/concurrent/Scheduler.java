package com.jd.journalq.toolkit.concurrent;

import com.jd.journalq.toolkit.lang.Close;
import com.jd.journalq.toolkit.lang.LifeCycle;
import com.jd.journalq.toolkit.service.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 延迟调度
 */
public class Scheduler extends Service {

    // 线程数
    private int threads;
    // 名称
    private String name;
    // 调度服务
    private ScheduledExecutorService scheduler;

    public Scheduler(final int threads) {
        this(threads, null);
    }

    public Scheduler(final int threads, final String name) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be greater than 0");
        }
        this.name = name == null || name.isEmpty() ? "Scheduler" : name;
        this.threads = threads;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        scheduler = Executors.newScheduledThreadPool(threads,
                new NamedThreadFactory(name == null ? Scheduler.class.getSimpleName() : name));
    }

    @Override
    protected void doStop() {
        Close.close(scheduler);
        super.doStop();
    }

    /**
     * 延迟执行
     *
     * @param command 命令
     * @param delay   延迟时间(毫秒)
     */
    public void schedule(final Runnable command, final long delay) {
        if (command != null) {
            scheduler.schedule(command, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 按照固定延迟时间调度
     *
     * @param command      命令
     * @param initialDelay 初始化延迟
     * @param delay        固定延迟时间
     */
    public void scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay) {
        if (command != null) {
            scheduler.scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 按照固定时间频率调度
     *
     * @param command      命令
     * @param initialDelay 初始化延迟
     * @param period       时间频率
     */
    public void scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period) {
        if (command != null) {
            scheduler.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 延迟关闭
     *
     * @param service 关闭
     * @param delay   延迟时间(毫秒)
     */
    public void close(final LifeCycle service, final long delay) {
        if (service != null) {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    service.stop();
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

}
