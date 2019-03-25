package com.jd.journalq.toolkit.time;

import java.util.concurrent.TimeUnit;

/**
 * 时间片段(纳秒)
 * Created by hexiaofeng on 16-7-16.
 */
public class NanoPeriod implements Period {
    // 开始时间
    protected long startTime;
    // 终止时间
    protected long endTime;

    @Override
    public void begin() {
        startTime = System.nanoTime();
    }

    @Override
    public void end() {
        endTime = System.nanoTime();
    }

    @Override
    public long time() {
        return endTime - startTime;
    }

    @Override
    public void clear() {
        startTime = 0;
        endTime = 0;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.NANOSECONDS;
    }
}
