package com.jd.journalq.toolkit.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名线程工厂类
 *
 * @author hexiaofeng
 * @since 2013-12-09
 */
public class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger counter = new AtomicInteger(0);

    private String name;
    private boolean daemon;

    public NamedThreadFactory(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        this.name = name;
    }

    public NamedThreadFactory(String name, boolean daemon) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(name + " - " + counter.incrementAndGet());
        if (daemon) {
            thread.setDaemon(daemon);
        }
        return thread;
    }

}
