package com.jd.journalq.toolkit.stat;


import com.jd.journalq.toolkit.time.SystemClock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 性能统计，读写双缓冲区<br/>
 * 可以设置时间间隔，在该时间间隔内多次的请求都只会返回一条
 */
public class TPStatDoubleBuffer<T extends TPStatSlice> {
    // 读缓冲区
    protected T readStat;
    // 写缓冲区
    protected T writeStat;
    // 时间间隔(毫秒)
    protected long interval;
    // 上次切片时间
    protected long lastTime;
    // 读写锁
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public TPStatDoubleBuffer(T readStat, T writeStat) {
        this(readStat, writeStat, 0);
    }

    public TPStatDoubleBuffer(T readStat, T writeStat, long interval) {
        this.readStat = readStat;
        this.writeStat = writeStat;
        this.interval = interval;
    }

    /**
     * 切片
     *
     * @return 当前可读取的数据
     */
    public T slice() {
        // 时间间隔
        if (!isExpire()) {
            return readStat;
        }
        lock.writeLock().lock();
        try {
            if (!isExpire()) {
                return readStat;
            }
            T stat = writeStat;
            writeStat = readStat;
            readStat = stat;

            if (writeStat != null) {
                // 有些场景不需要统计
                writeStat.clear();
                writeStat.getPeriod().begin();
            }
            lastTime = SystemClock.now();
            return readStat;
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * 是否过期
     *
     * @return 过期标示
     */
    protected boolean isExpire() {
        return interval <= 0 || lastTime <= 0 && (SystemClock.now() - lastTime > interval);
    }
}
